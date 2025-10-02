package lilac.rpcframework.remote.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
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
import org.springframework.beans.factory.annotation.Value;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static lilac.rpcframework.remote.constant.RpcConstant.REQUEST_TYPE;

@Slf4j
public class NettyClient {

    private final ServiceRegistry registry;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    @Value("${lilac.rpc.registry.type:zookeeper}")
    private static String registryType;
    @Value("${lilac.rpc.serialize.type:Hessian}")
    private static String codecType;
    @Value("${lilac.rpc.compress.type:gzip}")
    private static String compressType;

    public NettyClient() {

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
            InetSocketAddress address = registry.lookupService(request);
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
