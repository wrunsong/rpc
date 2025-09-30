package lilac.rpcframework.remote.transport;

import lilac.rpcframework.extension.SPI;
import lilac.rpcframework.remote.dto.RpcRequest;

@SPI
public interface RpcRequestTransport {

    /**
     * 客户端像服务端发起请求，并得到响应
     * @param request
     * @return
     */
    Object sendRequest(RpcRequest request);
}
