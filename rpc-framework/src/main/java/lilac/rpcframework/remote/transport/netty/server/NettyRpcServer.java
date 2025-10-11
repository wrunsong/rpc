package lilac.rpcframework.remote.transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lilac.rpcframework.config.ShutdownHook;
import lilac.rpcframework.constants.Constants;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.provider.ServiceProvider;
import lilac.rpcframework.remote.transport.netty.codec.RpcMessageDecoder;
import lilac.rpcframework.remote.transport.netty.codec.RpcMessageEncoder;
import lilac.rpcframework.remote.transport.netty.server.handler.NettyRpcServerHandler;
import lilac.rpcframework.utils.threadpool.ThreadPoolFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NettyRpcServer {

    private static final int PORT = Constants.SERVER_PORT;
    private static final String registryType = Constants.REGISTRY_TYPE;

    private final ServiceProvider serviceProvider = Objects.requireNonNull(
            ExtensionLoader.getExtensionLoader(ServiceProvider.class)).getExtension(registryType);

    /**
     * 启动服务
     */
    public void start() {
        // clear registry
        ShutdownHook.getInstance().clearAll();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() / 2);
        DefaultEventLoopGroup handlerGroup = new DefaultEventLoopGroup(
                Runtime.getRuntime().availableProcessors() / 2,
                ThreadPoolFactory.createThreadPool("server-handler-pool")
        );

        try {
            String host = InetAddress.getLocalHost().getHostAddress();

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

            ChannelFuture future = serverBootstrap.bind(host, PORT).sync();

            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("init server exception: {}", e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            handlerGroup.shutdownGracefully();
        }


    }
}
