package lilac.rpcframework.registry.nacos.util;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.listener.AbstractNamingChangeListener;
import com.alibaba.nacos.client.naming.listener.NamingChangeEvent;
import jakarta.annotation.PreDestroy;
import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NacosUtil {

    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private static final String registryAddress = yamlConfig.getLilacRpc().getRegistry().getAddress();
    private static final String namespace = yamlConfig.getLilacRpc().getRegistry().getNacos().getNamespace();
    private static final int maxRetries = yamlConfig.getLilacRpc().getRegistry().getMaxRetries();
    private static final int configRetryTime = yamlConfig.getLilacRpc().getRegistry().getNacos().getConfigRetryTime();
    private static final String username = yamlConfig.getLilacRpc().getRegistry().getNacos().getUsername();
    private static final String password = yamlConfig.getLilacRpc().getRegistry().getNacos().getPassword();

    private static final Set<String> REGISTERED_SERVICE_NAME = ConcurrentHashMap.newKeySet();
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();



    private static volatile NamingService nacosClient = null;

    private NacosUtil() {}


    /**
     * 获取Nacos客户端
     * @return
     */
    public static NamingService getNacosClient() {
        if (nacosClient != null && isClientConnected(nacosClient)) {
            return nacosClient;
        }

        Properties properties = new Properties();

        properties.setProperty(PropertyKeyConst.USERNAME, username);
        properties.setProperty(PropertyKeyConst.PASSWORD, password);
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, registryAddress);
        // namespace要么用public，要么用一串uuid
        properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);

        int maxRetries = 5;
        try {
            nacosClient = NacosFactory.createNamingService(properties);

            while (!isClientConnected(nacosClient) && maxRetries > 0) {
                log.info("Waiting for NacosClient to start");
                Thread.sleep(1000);
                maxRetries--;
            }
            if (!isClientConnected(nacosClient)) {
                log.error("NacosClient not connected");
                return null;
            }

        } catch (NacosException e) {
            log.error("Create naming service failed: {}", e.getMessage());
            return null;
        } catch (InterruptedException e) {
            log.error("Thread interrupted while waiting for NacosClient to start");
            return null;
        }
        return nacosClient;
    }

    /**
     * 在nacos上注册结点
     * @param nacosClient
     * @param serviceName
     * @param groupName
     * @param ip
     * @param port
     */
    public static void registerInstance(NamingService nacosClient, String serviceName, String groupName,
                                        String ip, int port) {
        try {
            if (REGISTERED_SERVICE_NAME.contains(serviceName) || !nacosClient.getAllInstances(serviceName, groupName).isEmpty()) {
                log.info("service {} already exists for group {} in nacos.", serviceName, groupName);
            } else {
                nacosClient.registerInstance(serviceName, groupName, ip, port);
            }
            REGISTERED_SERVICE_NAME.add(serviceName);

            List<String> list = SERVICE_ADDRESS_MAP.getOrDefault(serviceName, new ArrayList<>());
            list.add(ip + ":" + port);
            SERVICE_ADDRESS_MAP.put(serviceName, list);

        } catch (NacosException e) {
            log.error("register service failed: {}", e.getMessage());
        }
    }

    /**
     * 获取能够提供服务的IP:port
     * @param nacosClient
     * @param rpcServiceName
     * @return
     */
    public static List<String> getServiceAddresses(NamingService nacosClient, String rpcServiceName, String groupName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }

        try {
            List<Instance> instances = nacosClient.getAllInstances(rpcServiceName, groupName);
            List<String> addresses = instances.stream()
                    .map(instance -> instance.getIp() + ":" + instance.getPort())
                    .toList();
            SERVICE_ADDRESS_MAP.putIfAbsent(rpcServiceName, addresses);
            nacosWatcher(nacosClient, rpcServiceName, groupName);
            return addresses;
        } catch (NacosException e) {
            log.error("get instances failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 对nacos结点的变化进行监听
     * @param nacosClient
     * @param serviceName
     * @param groupName
     */
    private static void nacosWatcher(NamingService nacosClient, String serviceName, String groupName) {
        try {
            AbstractNamingChangeListener listener = new AbstractNamingChangeListener() {
                @Override
                public void onChange(NamingChangeEvent event) {
                    if (event.isAdded()) {
                        log.info("service {} has been added", serviceName);
                    } else if (event.isRemoved()) {
                        log.info("service {} has been removed", serviceName);
                    } else if (event.isModified()) {
                        log.info("service {} has been modified", serviceName);
                    }

                    if (event.getServiceName().equals(serviceName)) {
                        List<String> addresses = event.getInstances().stream().map(instance -> instance.getIp() + ":" + instance.getPort()).toList();
                        SERVICE_ADDRESS_MAP.put(serviceName, addresses);
                    }
                }
            };
            nacosClient.subscribe(serviceName, groupName, listener);
        } catch (NacosException e) {
            log.error("subscribe service failed: {}", e.getMessage());
        }
    }


    /**
     * 服务停止时，清除掉注册的服务
     * @param nacosClient
     * @param address
     */
    public static void clearNacosRegister(NamingService nacosClient, InetSocketAddress address) {
        String ip = address.getAddress().getHostAddress();
        int port = address.getPort();
        String addressString = (ip.equals("127.0.0.1") ? "localhost" : ip) + ":" + port;

        for (String serviceName : SERVICE_ADDRESS_MAP.keySet()) {
            log.info("address: {}, service {} has been deleted", addressString, serviceName);
            if (SERVICE_ADDRESS_MAP.get(serviceName).contains(addressString)) {
                SERVICE_ADDRESS_MAP.get(serviceName).remove(addressString);
                try {
                    nacosClient.deregisterInstance(serviceName, "lilac", ip, port);
                } catch (NacosException e) {
                    log.error("deregister service failed: {}", e.getMessage());
                }
            }
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            nacosClient.shutDown();
        } catch (NacosException e) {
            log.error("shutdown nacosClient failed: {}", e.getMessage());
        }
    }

    /**
     * 检查 Nacos 客户端是否已连接
     *
     * @param nacosClient
     * @return true 如果客户端连接正常
     */
    private static boolean isClientConnected(NamingService nacosClient) {
        try {
            // 尝试获取实例以验证连接是否正常
            nacosClient.getAllInstances("nacos-test");
            return true; // 如果没有异常，认为已连接
        } catch (Exception e) {
            return false; // 如果抛出异常，说明未连接
        }
    }

}
