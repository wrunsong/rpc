package lilac.rpcframework.loadbalance;

import lilac.rpcframework.extension.SPI;
import lilac.rpcframework.remote.dto.RpcRequest;

import java.util.List;

@SPI
public interface LoadBalance {

    /**
     * 负载均衡，找出合适的提供服务的服务端IP
     * @param serviceAddresses 提供服务的IP列表
     * @param request 请求
     * @return 选中的IP
     */
    String selectServiceAddress(List<String> serviceAddresses, RpcRequest request, String clientAddress);
}
