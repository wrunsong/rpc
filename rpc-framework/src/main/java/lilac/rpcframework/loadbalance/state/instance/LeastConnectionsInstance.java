package lilac.rpcframework.loadbalance.state.instance;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 最小连接数的服务状态对象
 */
public class LeastConnectionsInstance {
    // 关键：线程安全地维护当前活跃连接数
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    public int getAndIncrementActiveConnections() {
        return activeConnections.incrementAndGet();
    }

    public int getAndDecreaseActiveConnections() {
        return activeConnections.decrementAndGet();
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }


}