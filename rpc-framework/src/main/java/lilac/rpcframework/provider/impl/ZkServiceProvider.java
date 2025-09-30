package lilac.rpcframework.provider.impl;

import lilac.rpcframework.config.RpcServiceConfig;
import lilac.rpcframework.enums.ServiceRegistryType;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.provider.ServiceProvider;
import lilac.rpcframework.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkServiceProvider implements ServiceProvider {

    private final Map<String, Object> SERVICE_MAP;
    private final ServiceRegistry serviceRegistry;

    @Value("${lilac.rpc.server.port:8080}")
    private static int port;

    public ZkServiceProvider() {
        this.SERVICE_MAP = new ConcurrentHashMap<>();
        this.serviceRegistry = Objects.requireNonNull(ExtensionLoader.getExtensionLoader(ServiceRegistry.class))
                .getExtension(ServiceRegistryType.REGISTRY.getType());
    }

    /**
     * 将服务缓存到Map中
     * @param config
     */
    @Override
    public void addService(RpcServiceConfig config) {

        String rpcServiceName = config.getRpcServiceName();
        if (SERVICE_MAP.containsKey(rpcServiceName)) {
            return;
        }

        SERVICE_MAP.put(rpcServiceName, config.getService());
    }

    /**
     * 获取能够提供服务的类
     * @param rpcServiceName
     * @return
     */
    @Override
    public Object getService(String rpcServiceName) {
        if (rpcServiceName == null || rpcServiceName.isEmpty()) {
            log.error("rpcServiceName is null or empty");
            return null;
        }
        Object service = SERVICE_MAP.get(rpcServiceName);
        if (service == null) {
            log.error("There is no such service {}", rpcServiceName);
            return null;
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig config) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(config);
            serviceRegistry.register(config.getRpcServiceName(), new InetSocketAddress(host, port));
        } catch (Exception e) {
            log.error("publish service error: {}", e.getMessage());
        }

    }
}
