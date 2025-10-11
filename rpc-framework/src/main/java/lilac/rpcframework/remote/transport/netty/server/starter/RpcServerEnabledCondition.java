package lilac.rpcframework.remote.transport.netty.server.starter;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

// 只有在配置文件标明rpc.server.enabled=true才会启用autoStarter
// 不然在客户端，服务端在一台电脑上进行模拟时，监听器既监听服务端启动也监听客户端启动，会导致RPC服务端start两次
public class RpcServerEnabledCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 获取配置文件中的 `rpc.server.enabled` 属性
        String rpcServerEnabled = context.getEnvironment().getProperty("rpc.server.enabled");
        // 返回是否启用服务端
        return Boolean.parseBoolean(rpcServerEnabled);    }
}
