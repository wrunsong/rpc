package lilac.rpcframework.loadbalance.impl;

import lilac.rpcframework.loadbalance.AbstractLoadBalance;
import lilac.rpcframework.loadbalance.state.ServerState;
import lilac.rpcframework.loadbalance.state.instance.LeastConnectionsInstance;
import lilac.rpcframework.remote.dto.RpcRequest;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// 最小连接数 pick-2 算法 负载均衡
// TODO 改成并发安全版
public class LeastConnectionsLoadBalance extends AbstractLoadBalance {

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest request, String clientAddress) {

        // 1. 获取包含状态的实例列表
        List<LeastConnectionsInstance> instances = serviceAddresses.stream()
                .map(address ->
                        ServerState.SERVER_CONNECTION_COUNTER.computeIfAbsent(address, key -> new LeastConnectionsInstance())
                )
                .toList();

        int size = instances.size();
        if (size == 1) {
            // 只有一个实例时直接返回并递增
            instances.getFirst().getAndIncrementActiveConnections();
            return serviceAddresses.getFirst();
        }

        // 2. 随机选取两个索引 a 和 b
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int a =  random.nextInt(size);
        int b = random.nextInt(size);
        // 确保选取两个不同的实例（尽管相同逻辑也正确，但效率低）
        while (a == b) {
            b = random.nextInt(size);
        }

        LeastConnectionsInstance instanceA = instances.get(a);
        LeastConnectionsInstance instanceB = instances.get(b);
        String addressA = serviceAddresses.get(a);
        String addressB = serviceAddresses.get(b);

        // 3. 比较连接数并选择连接数更小的实例
        if (instanceA.getActiveConnections() >= instanceB.getActiveConnections()) {
            instanceB.getAndIncrementActiveConnections();
            return addressB;
        } else {
            instanceA.getAndIncrementActiveConnections();
            return addressA;
        }

    }

    /**
     * 将选中的服务地址的连接数-1
     * @param serviceAddress
     */
    public static void decreaseActiveConnections(String serviceAddress) {
        ServerState.SERVER_CONNECTION_COUNTER.computeIfPresent(serviceAddress, (key, value) -> {
            value.getAndDecreaseActiveConnections();
            return value;
        });
    }

}
