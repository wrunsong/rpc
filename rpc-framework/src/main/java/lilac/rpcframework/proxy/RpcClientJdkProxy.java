package lilac.rpcframework.proxy;

import lilac.rpcframework.config.RpcServiceConfig;
import lilac.rpcframework.enums.RpcErrorMessage;
import lilac.rpcframework.enums.RpcResponseCode;
import lilac.rpcframework.remote.dto.RpcRequest;
import lilac.rpcframework.remote.dto.RpcResponse;
import lilac.rpcframework.remote.transport.netty.client.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClientJdkProxy implements InvocationHandler, RpcClientProxy {

    private NettyClient nettyClient;
    private RpcServiceConfig rpcServiceConfig;


    @Override
    public void setClient(NettyClient nettyClient, RpcServiceConfig rpcServiceConfig) {
        this.nettyClient = nettyClient;
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

        RpcRequest rpcRequest = getRpcRequest(method, args);

        CompletableFuture<RpcResponse<Object>> future = nettyClient.sendRpcRequest(rpcRequest);

        RpcResponse<Object> rpcResponse = future.join();

        if (!check(rpcRequest, rpcResponse)) {
            return null;
        }

        return rpcResponse.getData();
    }


    private RpcRequest getRpcRequest(Method method,  Object[] args) {
        return RpcRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
    }

    private boolean check(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        if (rpcResponse == null) {
            log.error("{}: interfaceName :{}", RpcErrorMessage.SERVICE_INVOCATION_FAILURE, rpcRequest.getInterfaceName());
            return false;
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            log.error("{}: interfaceName :{}", RpcErrorMessage.REQUEST_NOT_MATCH_RESPONSE, rpcRequest.getInterfaceName());
            return false;
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
            log.error("{}: interfaceName :{}", RpcErrorMessage.SERVICE_INVOCATION_FAILURE, rpcRequest.getInterfaceName());
            return false;
        }
        return true;
    }
}
