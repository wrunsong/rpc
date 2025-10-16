package lilac.rpcframework.proxy.impl;

import lilac.rpcframework.config.RpcServiceConfig;
import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.proxy.RpcClientProxy;
import lilac.rpcframework.remote.dto.RpcRequest;
import lilac.rpcframework.remote.dto.RpcResponse;
import lilac.rpcframework.remote.transport.netty.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClientCglibProxy implements RpcClientProxy, MethodInterceptor {

    private NettyRpcClient nettyRpcClient;
    private RpcServiceConfig rpcServiceConfig;
    private final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private final String loadBalanceType = yamlConfig.getLilacRpc().getLoadbalance().getType();

    @Override
    public void setClient(NettyRpcClient nettyRpcClient, RpcServiceConfig rpcServiceConfig) {
        this.nettyRpcClient = nettyRpcClient;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    /**
     * 获取输入类的代理对象
     * @param clazz
     * @return
     * @param <T>
     */
    @Override
    public <T> T getProxy(Class<T> clazz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);  // 设置父类
        enhancer.setCallback(this);     // 设置方法拦截器
        return (T) enhancer.create();   // 创建代理对象
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {

        long start = System.nanoTime();
        RpcRequest rpcRequest = getRpcRequest(method, args,  rpcServiceConfig);

        CompletableFuture<RpcResponse<Object>> future = nettyRpcClient.sendRpcRequest(rpcRequest);

        return processFutureResponse(future, start, rpcRequest, nettyRpcClient, loadBalanceType);

    }

}
