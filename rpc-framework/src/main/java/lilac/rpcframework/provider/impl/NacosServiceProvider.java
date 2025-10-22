package lilac.rpcframework.provider.impl;

import lilac.rpcframework.config.RpcServiceConfig;
import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.provider.ServiceProvider;
import lilac.rpcframework.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
// TODO 和ZkServiceProvider合并，没区别
public class NacosServiceProvider implements ServiceProvider {

    private final Map<String, Object> SERVICE_MAP;
    private final ServiceRegistry serviceRegistry;

    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private static final String RPC_SERVER_ADDRESS = yamlConfig.getLilacRpc().getServerAddress();
    private static final int RPC_SERVER_PORT = yamlConfig.getLilacRpc().getServerPort();
    private static final String registryType = yamlConfig.getLilacRpc().getRegistry().getType();

    public NacosServiceProvider() {
        this.SERVICE_MAP = new ConcurrentHashMap<>();
        this.serviceRegistry = Objects.requireNonNull(ExtensionLoader.getExtensionLoader(ServiceRegistry.class))
                .getExtension(registryType);
    }

    @Override
    public void addService(RpcServiceConfig config) {

        String rpcServiceName = config.getFullyExposeName();
        if (SERVICE_MAP.containsKey(rpcServiceName)) {
            return;
        }
        SERVICE_MAP.put(rpcServiceName, config.getService());
    }

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
            this.addService(config);
            serviceRegistry.register(config.getFullyExposeName(), new InetSocketAddress(RPC_SERVER_ADDRESS, RPC_SERVER_PORT));
        } catch (Exception e) {
            log.error("publish service error: {}", e.getMessage());
        }
    }
}
