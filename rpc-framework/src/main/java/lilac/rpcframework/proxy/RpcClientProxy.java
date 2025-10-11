package lilac.rpcframework.proxy;

import lilac.rpcframework.config.RpcServiceConfig;
import lilac.rpcframework.extension.SPI;
import lilac.rpcframework.remote.transport.netty.client.NettyRpcClient;

@SPI
public interface RpcClientProxy {

    void setClient(NettyRpcClient nettyRpcClient, RpcServiceConfig rpcServiceConfig);

    <T> T getProxy(Class<T> clazz);

}
