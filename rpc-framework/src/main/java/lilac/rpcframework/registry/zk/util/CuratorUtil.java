package lilac.rpcframework.registry.zk.util;


import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * zk util
 */
@Slf4j
public class CuratorUtil {

    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private static final int BASE_SLEEP_TIME = yamlConfig.getLilacRpc().getRegistry().getZk().getBaseSleepTime();
    private static final int MAX_RETRIES = yamlConfig.getLilacRpc().getRegistry().getMaxRetries();
    private static final String ZK_REGISTER_ROOT_PATH = yamlConfig.getLilacRpc().getRegistry().getZk().getPath();
    private static final String ZOOKEEPER_ADDRESS = yamlConfig.getLilacRpc().getRegistry().getAddress();
    // serviceName -> addresses
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    // full zk path
    private static final Set<String> REGISTERED_PATH = ConcurrentHashMap.newKeySet();

    private static CuratorFramework zkClient;

    private CuratorUtil() {}


    /**
     * 获取zk客户端
     * @return
     */
    public static CuratorFramework getZkClient() {
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }

        ExponentialBackoffRetry policy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);

        zkClient = CuratorFrameworkFactory.builder()
                .connectString(ZOOKEEPER_ADDRESS)
                .retryPolicy(policy)
                .build();
        zkClient.start();

        try {
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                log.error("zk client connect error");
                return null;
            }
        } catch (InterruptedException e) {
            log.error("zk client connect error: {}", e.getMessage());
            return null;
        }
        return zkClient;
    }

    /**
     * 创建zk永久结点
     * @param zkClient 客户端
     * @param zkPath zk路径
     */
    public static void createPersistentNode(CuratorFramework zkClient, String zkPath) {
        if (!zkPath.startsWith(ZK_REGISTER_ROOT_PATH)) {
            zkPath = ZK_REGISTER_ROOT_PATH + "/" + zkPath;
        }
        try {
            if (REGISTERED_PATH.contains(zkPath) || zkClient.checkExists().forPath(zkPath) != null) {
                log.info("zk node already exists for path: {}", zkPath);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkPath);
            }
            REGISTERED_PATH.add(zkPath);
        }  catch (Exception e) {
            log.error("create zk node for path : {} failed. Error message: {}", zkClient, e.getMessage());
        }
    }

    /**
     *  获取rpcServiceName对应的zk路径下的所有可以提供服务的子节点（也就是IP地址）
     * @param zkClient
     * @param rpcServiceName
     * @return
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> addresses = new ArrayList<>();
        String zkPath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;

        try {
            addresses = zkClient.getChildren().forPath(zkPath);
            SERVICE_ADDRESS_MAP.putIfAbsent(rpcServiceName, addresses);
            zkWatcher(zkClient, zkPath);
        }  catch (Exception e) {
            log.error("get zk node for path: {} failed. Error message: {}", zkPath, e.getMessage());
        }
        return addresses;
    }


    /**
     * 注册children node的监听器，一旦注册的IP发生改变，就及时更新缓存
     * @param zkClient
     * @param zkPath
     */
    private static void zkWatcher(CuratorFramework zkClient, String zkPath) {
        try {
            PathChildrenCache childrenCache = new PathChildrenCache(zkClient, zkPath, true);

            PathChildrenCacheListener childrenCacheListener = (client, event) -> {
                List<String> addresses = client.getChildren().forPath(zkPath);
                // 这就相当于只要addresses有变化，就会put进缓存
                SERVICE_ADDRESS_MAP.put(zkPath, addresses);
            };

            childrenCache.getListenable().addListener(childrenCacheListener);
            childrenCache.start();
        } catch (Exception e) {
            log.error("zk node [{}] listener error: {}", zkPath, e.getMessage());
        }
    }


    /**
     * 按照ip清理zk
     * @param zkClient
     * @param address
     */
    public static void clearZkRegistry(CuratorFramework zkClient, InetSocketAddress address) {
        REGISTERED_PATH.parallelStream().forEach(zkPath -> {
            try {
                if (zkPath.endsWith(address.toString())) {
                    zkClient.delete().forPath(zkPath);
                }
            } catch (Exception e) {
                log.error("delete zk node for path: {} failed. Error message: {}", zkPath, e.getMessage());
            }
        });
    }



}
