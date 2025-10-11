package lilac.rpcframework.remote.transport.netty.server.starter;

import lilac.rpcframework.remote.transport.netty.server.NettyRpcServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Conditional(RpcServerEnabledCondition.class)
public class RpcServerAutoStarter implements ApplicationListener<ContextRefreshedEvent> {

    private final NettyRpcServer nettyRpcServer;

    public RpcServerAutoStarter(NettyRpcServer nettyRpcServer) {
        this.nettyRpcServer = nettyRpcServer;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 监听到spring容器完全启动后，自动执行
        nettyRpcServer.start();
    }
}
