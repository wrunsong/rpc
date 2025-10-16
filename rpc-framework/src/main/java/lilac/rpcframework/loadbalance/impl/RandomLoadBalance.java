package lilac.rpcframework.loadbalance.impl;

import lilac.rpcframework.loadbalance.AbstractLoadBalance;
import lilac.rpcframework.remote.dto.RpcRequest;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest request, String clientAddress) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
