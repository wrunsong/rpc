package lilac.rpcframework.config;

import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class ShutdownHook {
    // lazy mode singleton
    private static volatile ShutdownHook shutdownHook = null;
    private final ServiceRegistry serviceRegistry;

    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private static final String registryType = yamlConfig.getLilacRpc().getRegistry().getType();

    private ShutdownHook() {
        this.serviceRegistry = Objects.requireNonNull(ExtensionLoader.getExtensionLoader(ServiceRegistry.class))
                .getExtension(registryType);
    }


    public static ShutdownHook getInstance() {
        if (shutdownHook == null) {
            synchronized (ShutdownHook.class) {
                if (shutdownHook == null) {
                    shutdownHook = new ShutdownHook();
                }
            }
        }
        return shutdownHook;
    }

    public void clearAll() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            serviceRegistry.clearRegistry();
        }));
    }
}
