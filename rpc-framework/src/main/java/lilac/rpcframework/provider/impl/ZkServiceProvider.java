package lilac.rpcframework.provider.impl;

import lilac.rpcframework.config.RpcServiceConfig;
import lilac.rpcframework.constants.Constants;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.provider.ServiceProvider;
import lilac.rpcframework.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkServiceProvider implements ServiceProvider {

    private final Map<String, Object> SERVICE_MAP;
    private final ServiceRegistry serviceRegistry;


    private static final int port = Constants.SERVER_PORT;
    private static final String registryType = Constants.REGISTRY_TYPE;

    public ZkServiceProvider() {
        this.SERVICE_MAP = new ConcurrentHashMap<>();
        this.serviceRegistry = Objects.requireNonNull(ExtensionLoader.getExtensionLoader(ServiceRegistry.class))
                .getExtension(registryType);
    }

    /**
     * 将服务缓存到Map中
     * @param config
     */
    @Override
    public void addService(RpcServiceConfig config) {

        String rpcServiceName = config.getFullyExposeName();
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
            serviceRegistry.register(config.getFullyExposeName(), new InetSocketAddress(host, port));
        } catch (Exception e) {
            log.error("publish service error: {}", e.getMessage());
        }

    }
}
