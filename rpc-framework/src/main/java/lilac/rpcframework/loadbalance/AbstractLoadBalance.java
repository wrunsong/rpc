package lilac.rpcframework.loadbalance;

import lilac.rpcframework.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest request, String clientAddress) {
        if (serviceAddresses == null || serviceAddresses.isEmpty()) {
            log.error("service address is empty");
            return null;
        }
        if (request == null) {
            log.error("request is null");
            return null;
        }
//        if (serviceAddresses.size() == 1) {
//            return serviceAddresses.getFirst();
//        }

        return doSelect(serviceAddresses, request, clientAddress);
    }


    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest request, String clientAddress);
}
