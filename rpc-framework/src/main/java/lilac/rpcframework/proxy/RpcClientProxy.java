package lilac.rpcframework.proxy;

import lilac.rpcframework.config.RpcServiceConfig;
import lilac.rpcframework.enums.RpcErrorMessage;
import lilac.rpcframework.enums.RpcResponseCode;
import lilac.rpcframework.extension.SPI;
import lilac.rpcframework.loadbalance.impl.LeastConnectionsLoadBalance;
import lilac.rpcframework.loadbalance.impl.LeastTimeLoadBalance;
import lilac.rpcframework.remote.dto.RpcRequest;
import lilac.rpcframework.remote.dto.RpcResponse;
import lilac.rpcframework.remote.transport.netty.client.NettyRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static lilac.rpcframework.config.Constants.LEAST_CONNECTIONS;
import static lilac.rpcframework.config.Constants.LEAST_TIME;

@SPI
public interface RpcClientProxy {

    void setClient(NettyRpcClient nettyRpcClient, RpcServiceConfig rpcServiceConfig);

    <T> T getProxy(Class<T> clazz);



    default RpcRequest getRpcRequest(Method method, Object[] args, RpcServiceConfig rpcServiceConfig) {
        return RpcRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .serviceName(method.getDeclaringClass().getSimpleName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(castClassToString(method.getParameterTypes()))
                .returnType(method.getReturnType().getName())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
    }

    private String[] castClassToString(Class<?>[] paramTypes) {
        String[] paramTypesStr = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramTypesStr[i] = paramTypes[i].getName();
        }
        return paramTypesStr;
    }


    default boolean check(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        Logger log = LoggerFactory.getLogger(this.getClass());
        if (rpcResponse == null) {
            log.error("{}: interfaceName :{}", RpcErrorMessage.SERVICE_INVOCATION_FAILURE, rpcRequest.getServiceName());
            return false;
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            log.error("{}: interfaceName :{}", RpcErrorMessage.REQUEST_NOT_MATCH_RESPONSE, rpcRequest.getServiceName());
            return false;
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCode.SUCCESS.getCode())) {
            log.error("{}: interfaceName :{}", RpcErrorMessage.SERVICE_INVOCATION_FAILURE, rpcRequest.getServiceName());
            return false;
        }
        return true;
    }

    default Object processFutureResponse(CompletableFuture<RpcResponse<Object>> future, long startTime,
                                         RpcRequest rpcRequest, NettyRpcClient nettyRpcClient, String loadBalanceType) {

        Logger log = LoggerFactory.getLogger(this.getClass());
        RpcResponse<Object> rpcResponse = null;

        long latencyMs = -1;

        try {
            rpcResponse = future.join();

            long end = System.nanoTime();
            latencyMs = end - startTime;

            if (!check(rpcRequest, rpcResponse)) {
                return null;
            }

            return rpcResponse.getData();
        } catch (Exception e) {
            long end = System.nanoTime();
            latencyMs = end - startTime;
            log.error("invoke rpc exception: {}", e.getMessage());
            return null;
        } finally {
            if (rpcResponse == null) {
                log.error("rpc response is null in JDK proxy.");
                nettyRpcClient.close();
            } else {
                if (loadBalanceType.equals(LEAST_CONNECTIONS)) {
                    String serviceAddress = rpcResponse.getServiceAddress();
                    LeastConnectionsLoadBalance.decreaseActiveConnections(serviceAddress);
                } else if (loadBalanceType.equals(LEAST_TIME)) {
                    String serviceAddress = rpcResponse.getServiceAddress();
                    LeastTimeLoadBalance.updateLatency(serviceAddress, latencyMs);
                }
            }
        }
    }

}
