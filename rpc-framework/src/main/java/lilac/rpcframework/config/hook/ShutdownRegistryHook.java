package lilac.rpcframework.config.hook;

import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class ShutdownRegistryHook {
    // lazy mode singleton
    private static volatile ShutdownRegistryHook shutdownRegistryHook = null;
    private final ServiceRegistry serviceRegistry;

    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private static final String registryType = yamlConfig.getLilacRpc().getRegistry().getType();

    private ShutdownRegistryHook() {
        this.serviceRegistry = Objects.requireNonNull(ExtensionLoader.getExtensionLoader(ServiceRegistry.class))
                .getExtension(registryType);
    }


    public static ShutdownRegistryHook getInstance() {
        if (shutdownRegistryHook == null) {
            synchronized (ShutdownRegistryHook.class) {
                if (shutdownRegistryHook == null) {
                    shutdownRegistryHook = new ShutdownRegistryHook();
                }
            }
        }
        return shutdownRegistryHook;
    }

    public void clearAll() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serviceRegistry.clearRegistry();
        }));
    }
}
