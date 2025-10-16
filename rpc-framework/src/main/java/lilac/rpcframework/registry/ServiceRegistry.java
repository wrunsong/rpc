package lilac.rpcframework.registry;

import lilac.rpcframework.extension.SPI;
import lilac.rpcframework.remote.dto.RpcRequest;

import java.net.InetSocketAddress;

@SPI
public interface ServiceRegistry {

    /**
     * 在注册中心注册服务
     * @param rpcServiceName 服务名称 methodName + group + version
     * @param inetSocketAddress IP
     */
    void register(String rpcServiceName, InetSocketAddress inetSocketAddress);


    /**
     *  在注册中心发现服务
     * @param request 客户端发起的请求
     * @return 能够提供服务的IP
     */
    InetSocketAddress lookupService(RpcRequest request, String clientAddress);

    /**
     * 在关闭服务的时候，同时清理注册中心注册的服务
     */
    void clearRegistry();
}
