package lilac.rpcframework.proxy;

import lilac.rpcframework.config.RpcServiceConfig;
import lilac.rpcframework.extension.SPI;
import lilac.rpcframework.remote.transport.netty.client.NettyClient;

@SPI
public interface RpcClientProxy {

    void setClient(NettyClient nettyClient, RpcServiceConfig rpcServiceConfig);

    <T> T getProxy(Class<T> clazz);

}
