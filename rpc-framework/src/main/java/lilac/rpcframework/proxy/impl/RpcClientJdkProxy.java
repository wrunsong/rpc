package lilac.rpcframework.proxy.impl;

import lilac.rpcframework.config.RpcServiceConfig;
import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.proxy.RpcClientProxy;
import lilac.rpcframework.remote.dto.RpcRequest;
import lilac.rpcframework.remote.dto.RpcResponse;
import lilac.rpcframework.remote.transport.netty.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClientJdkProxy implements InvocationHandler, RpcClientProxy {

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
     * 获取输入类型的代理对象
     * @param clazz
     * @return
     * @param <T>
     */
    @Override
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        long start = System.nanoTime();
        RpcRequest rpcRequest = getRpcRequest(method, args, rpcServiceConfig);

        CompletableFuture<RpcResponse<Object>> future = nettyRpcClient.sendRpcRequest(rpcRequest);

        return processFutureResponse(future, start, rpcRequest, nettyRpcClient, loadBalanceType);
    }

}
