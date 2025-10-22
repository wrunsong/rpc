package lilac.server.controller;

import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.registry.ServiceRegistry;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/test/register")
public class RegisterController {

    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private static final String RPC_SERVER_ADDRESS = yamlConfig.getLilacRpc().getServerAddress();
    private static final int RPC_SERVER_PORT = yamlConfig.getLilacRpc().getServerPort();
    private static final String registryType = yamlConfig.getLilacRpc().getRegistry().getType();

    @DeleteMapping("/clear")
    public void clearTest() {
        ServiceRegistry serviceRegistry = Objects.requireNonNull(ExtensionLoader.getExtensionLoader(ServiceRegistry.class))
                .getExtension(registryType);

        serviceRegistry.clearRegistry();
    }
}
