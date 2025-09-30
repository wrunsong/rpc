package lilac.rpcframework.utils.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class ThreadPoolFactory {

    // threadPoolName -> ThreadPool
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactory() {}

    public static ExecutorService createThreadPool(String threadPoolName) {
        ThreadPoolConfig threadPoolConfig = new ThreadPoolConfig();
        return createThreadPool(threadPoolName, threadPoolConfig, false);
    }

    public static ExecutorService createThreadPool(String threadPoolName, ThreadPoolConfig threadPoolConfig, boolean daemon) {
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadPoolName,
                key -> innerCreateThreadPool(key, threadPoolConfig, daemon));

        if (threadPool.isShutdown() ||  threadPool.isTerminated()) {
            THREAD_POOLS.remove(threadPoolName);
            threadPool = THREAD_POOLS.computeIfAbsent(threadPoolName, key -> innerCreateThreadPool(key, threadPoolConfig, daemon));
        }
        return threadPool;
    }


    private static ExecutorService innerCreateThreadPool(String threadPoolName, ThreadPoolConfig threadPoolConfig, boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadPoolName, daemon);
        return new ThreadPoolExecutor(
                threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaximumPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                threadPoolConfig.getUnit(),
                threadPoolConfig.getWorkQueue(),
                threadFactory
        );
    }

    private static ThreadFactory createThreadFactory(String threadPoolName, boolean daemon) {
        if (threadPoolName != null && !threadPoolName.isEmpty()) {
            return new ThreadFactoryBuilder()
                    .setDaemon(daemon)
                    .setNameFormat(threadPoolName + "-%d")
                    .build();
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * 优雅的关闭所有线程池
     */
    public static void shutdownAllThreadPools() {
        log.info("shutdown all thread pools");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService threadPool = entry.getValue();
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("shutdown thread pool error: {}", e.getMessage());
                threadPool.shutdownNow();
            }
        });
    }

    /**
     * 优雅的关闭某个特定线程池
     * @param threadPoolName
     */
    public static void shutdownThreadPool(String threadPoolName) {
        ExecutorService threadPool = THREAD_POOLS.get(threadPoolName);
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("shutdown thread pool [{}] error: {}", threadPoolName, e.getMessage());
                threadPool.shutdownNow();
            }
        }
    }
}
