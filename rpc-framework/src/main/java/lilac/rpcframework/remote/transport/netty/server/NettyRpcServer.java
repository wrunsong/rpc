package lilac.rpcframework.remote.transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import lilac.rpcframework.config.hook.ShutdownRegistryHook;
import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.provider.ServiceProvider;
import lilac.rpcframework.remote.transport.netty.codec.RpcMessageDecoder;
import lilac.rpcframework.remote.transport.netty.codec.RpcMessageEncoder;
import lilac.rpcframework.remote.transport.netty.server.handler.NettyRpcServerHandler;
import lilac.rpcframework.utils.threadpool.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NettyRpcServer {

    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private static final String RPC_SERVER_ADDRESS = yamlConfig.getLilacRpc().getServerAddress();
    private static final int RPC_SERVER_PORT = yamlConfig.getLilacRpc().getServerPort();
    private static final String registryType = yamlConfig.getLilacRpc().getRegistry().getType();

    private final ServiceProvider serviceProvider = Objects.requireNonNull(
            ExtensionLoader.getExtensionLoader(ServiceProvider.class)).getExtension(registryType);


    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private DefaultEventLoopGroup handlerGroup;
    /**
     * 启动服务
     */
    public void start() {
        // clear registry
        ShutdownRegistryHook.getInstance().clearAll();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() / 2);
        handlerGroup = new DefaultEventLoopGroup(
                Runtime.getRuntime().availableProcessors() / 2,
                ThreadPoolFactory.createThreadPool("server-handler-pool")
        );

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new RpcMessageEncoder());
                            pipeline.addLast(new RpcMessageDecoder());
                            pipeline.addLast(handlerGroup, new NettyRpcServerHandler());
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(RPC_SERVER_ADDRESS, RPC_SERVER_PORT).sync();

            // TODO future.channel().closeFuture().sync();在关闭spring服务时会抛异常，去掉sync就不会了
            future.channel().closeFuture();
        } catch (Exception e) {
            log.error("server exception: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        // 只在非null时进行shutdown
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (handlerGroup != null) {
            handlerGroup.shutdownGracefully();
        }
        log.info("Netty EventLoopGroups shutdown completed.");
    }
}
