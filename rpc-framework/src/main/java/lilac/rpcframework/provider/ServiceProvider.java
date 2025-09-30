package lilac.rpcframework.provider;

import lilac.rpcframework.config.RpcServiceConfig;
import lilac.rpcframework.extension.SPI;

/**
 * service provider是提供给服务端用的，主要是起到缓存的作用
 * 当客户端的rpc请求过来，服务端会从provider类里找到合适的类提供服务
 */
@SPI
public interface ServiceProvider {

    /**
     * 增加服务
     * @param config
     */
    void addService(RpcServiceConfig config);

    /**
     * 获取服务
     * @param rpcServiceName
     * @return
     */
    Object getService(String rpcServiceName);

    /**
     * 将服务发布到注册中心
     * @param config
     */
    void publishService(RpcServiceConfig config);

}
