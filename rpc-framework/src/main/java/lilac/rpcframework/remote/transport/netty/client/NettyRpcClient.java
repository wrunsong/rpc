package lilac.rpcframework.remote.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.enums.CompressType;
import lilac.rpcframework.enums.SerializationType;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.registry.ServiceRegistry;
import lilac.rpcframework.remote.dto.RpcMessage;
import lilac.rpcframework.remote.dto.RpcRequest;
import lilac.rpcframework.remote.dto.RpcResponse;
import lilac.rpcframework.remote.transport.netty.client.handler.NettyRpcClientHandler;
import lilac.rpcframework.remote.transport.netty.client.utils.ChannelProvider;
import lilac.rpcframework.remote.transport.netty.client.utils.UnProcessedRequests;
import lilac.rpcframework.remote.transport.netty.codec.RpcMessageDecoder;
import lilac.rpcframework.remote.transport.netty.codec.RpcMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static lilac.rpcframework.remote.constant.RpcConstant.REQUEST_TYPE;

@Slf4j
public class NettyRpcClient {

    private final ServiceRegistry registry;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private static final String registryType = yamlConfig.getLilacRpc().getRegistry().getType();
    private static final String codecType = yamlConfig.getLilacRpc().getCodec().getType();
    private static final String compressType = yamlConfig.getLilacRpc().getCompress().getType();

    public NettyRpcClient() {

        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));

                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });

        registry = Objects.requireNonNull(
                ExtensionLoader.getExtensionLoader(ServiceRegistry.class)).getExtension(registryType);
    }

    /**
     * 和指定的服务器建立连接
     * @param address
     * @return
     */
    public Channel connect(InetSocketAddress address) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(address)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        completableFuture.complete(future.channel());
                    } else  {
                        completableFuture.completeExceptionally(future.cause());
                    }
                });
        return completableFuture.join();
    }

    /**
     * 尝试从缓存中获取Channel
     * @param address
     * @return
     */
    public Channel getChannel(InetSocketAddress address) {
        if (address == null) {
            log.error("address is null");
            return null;
        }
        Channel channel = ChannelProvider.getChannel(address);
        if (channel == null) {
            channel = connect(address);
            ChannelProvider.putChannel(address, channel);
        }
        return channel;
    }

    /**
     * 向服务端发起请求，并且返回一个CompletableFuture对象
     * @param request
     * @return
     */
    public CompletableFuture<RpcResponse<Object>> sendRpcRequest(RpcRequest request) {
        CompletableFuture<RpcResponse<Object>> responseFuture = new CompletableFuture<>();

        try {
            String clientAddress = InetAddress.getLocalHost().getHostAddress();
            InetSocketAddress address = registry.lookupService(request, clientAddress);
            Channel channel = getChannel(address);

            if (channel != null && channel.isActive()) {
                UnProcessedRequests.put(request.getRequestId(), responseFuture);
                RpcMessage rpcMessage = RpcMessage.builder()
                        .data(request)
                        .messageType(REQUEST_TYPE)
                        .codec(SerializationType.getCodeByType(codecType))
                        .compress(CompressType.getCodeByName(compressType))
                        .build();
                channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("send rpc request success");
                        // 启动超时定时任务
                        future.channel().eventLoop().schedule(() -> {
                            if (!responseFuture.isDone()) {
                                UnProcessedRequests.remove(request.getRequestId()); // 超时后移除未处理请求
                                log.warn("RPC request timeout, requestId: {}", request.getRequestId());
                            }
                        }, 5, TimeUnit.SECONDS);

                    } else {
                        log.error("Construct channel error: {}", future.cause().getMessage());
                        future.channel().close();
                        responseFuture.completeExceptionally(future.cause());
                        UnProcessedRequests.remove(request.getRequestId());
                    }
                });

            }
        } catch (Exception e) {
            log.error("send rpc request error: {}", e.getMessage());
            responseFuture.completeExceptionally(e);
            UnProcessedRequests.remove(request.getRequestId());
        }
        return responseFuture;
    }

    /**
     * 客户端关闭
     */
    public void close() {
        eventLoopGroup.shutdownGracefully();
    }

}
