package lilac.rpcframework.loadbalance.impl;

import lilac.rpcframework.loadbalance.AbstractLoadBalance;
import lilac.rpcframework.remote.dto.RpcRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance extends AbstractLoadBalance {
    private static final AtomicInteger INIT_COUNTER = new AtomicInteger(0);
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest request, String clientAddress) {
        int index = INIT_COUNTER.incrementAndGet();
        return serviceAddresses.get(Math.abs(index) % serviceAddresses.size());
    }
}
