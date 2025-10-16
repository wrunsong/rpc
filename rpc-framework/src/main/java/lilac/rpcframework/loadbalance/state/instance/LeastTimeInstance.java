package lilac.rpcframework.loadbalance.state.instance;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 最小响应时间的服务状态对象
 */
public class LeastTimeInstance {
    private final AtomicLong totalLatency = new AtomicLong(0);
    private final AtomicLong totalRequests = new AtomicLong(0);

    public synchronized void recordRequest(long latency) {
        totalLatency.addAndGet(latency);
        totalRequests.incrementAndGet();
    }

    // 获取平均响应时间
    public long getAverageRequestTime() {
        if (totalRequests.get() == 0) {
            return Long.MAX_VALUE;
        }
        return totalLatency.get() / totalRequests.get();
    }

    public long getTotalRequests() {
        return totalRequests.get();
    }
}
