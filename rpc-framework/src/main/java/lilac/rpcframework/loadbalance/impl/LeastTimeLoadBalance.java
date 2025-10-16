package lilac.rpcframework.loadbalance.impl;

import lilac.rpcframework.loadbalance.AbstractLoadBalance;
import lilac.rpcframework.loadbalance.state.ServerState;
import lilac.rpcframework.loadbalance.state.instance.LeastTimeInstance;
import lilac.rpcframework.remote.dto.RpcRequest;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// pick-2优化的最小时间 负载均衡
// TODO 改成并发安全版
public class LeastTimeLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest request, String clientAddress) {

        List<LeastTimeInstance> instances = serviceAddresses.stream()
                .map(address -> ServerState.SERVER_RESPONSE_TIME.computeIfAbsent(address, key -> new LeastTimeInstance()))
                .toList();

        int size = instances.size();
        if (size == 1) {
            return serviceAddresses.getFirst();
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int a = random.nextInt(size);
        int b = random.nextInt(size);
        while (a == b) {
            b = random.nextInt(size);
        }

        String addressA = serviceAddresses.get(a);
        String addressB = serviceAddresses.get(b);
        LeastTimeInstance instanceA = instances.get(a);
        LeastTimeInstance instanceB = instances.get(b);

        if (instanceA.getAverageRequestTime() < instanceB.getAverageRequestTime()) {
            return addressA;
        } else {
            return addressB;
        }
    }

    // 必须提供的 Hook 方法
    // =========================================================

    /**
     * 客户端调用完成后，更新指定地址的延迟指标。
     * 必须在 RPC 代理的 finally 或 whenComplete 块中调用。
     * @param address 选中的服务地址 (IP:Port)
     * @param latencyInMs 本次请求的耗时
     */
    public static void updateLatency(String address, long latencyInMs) {
        ServerState.SERVER_RESPONSE_TIME.computeIfPresent(address, (key, instance) -> {
            // 假设 LTInstance.recordRequest 是线程安全的（例如使用了 synchronized 或 AtomicLong）
            instance.recordRequest(latencyInMs);
            return instance;
        });
    }
}
