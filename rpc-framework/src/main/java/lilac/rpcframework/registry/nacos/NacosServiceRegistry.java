package lilac.rpcframework.registry.nacos;

import com.alibaba.nacos.api.naming.NamingService;
import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.loadbalance.LoadBalance;
import lilac.rpcframework.registry.ServiceRegistry;
import lilac.rpcframework.registry.nacos.util.NacosUtil;
import lilac.rpcframework.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {

    private final LoadBalance loadBalance;
    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private static final String loadBalanceType = yamlConfig.getLilacRpc().getLoadbalance().getType();
    private static final String RPC_SERVER_ADDRESS = yamlConfig.getLilacRpc().getServerAddress();
    private static final int RPC_SERVER_PORT = yamlConfig.getLilacRpc().getServerPort();

    public NacosServiceRegistry() {
        loadBalance = Objects.requireNonNull(ExtensionLoader.getExtensionLoader(LoadBalance.class))
                .getExtension(loadBalanceType);
    }

    @Override
    public void register(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        NamingService nacosClient = NacosUtil.getNacosClient();

        String[] strings = rpcServiceName.split(":group:", 2);
        String[] split = strings[1].split(",version:");
        String groupName = split[0];

        NacosUtil.registerInstance(nacosClient, rpcServiceName, groupName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest request, String clientAddress) {
        String rpcServiceName = request.getFullyExposeName();

        NamingService nacosClient = NacosUtil.getNacosClient();
        List<String> addresses = NacosUtil.getServiceAddresses(nacosClient, rpcServiceName, request.getGroup());
        if (addresses == null || addresses.isEmpty()) {
            log.error("no server can provide service: {}",  rpcServiceName);
        }
        String targetAddress = loadBalance.selectServiceAddress(addresses, request, clientAddress);
        String[] strings = targetAddress.split(":");
        String ipAddress = strings[0];
        int port = Integer.parseInt(strings[1]);
        return new InetSocketAddress(ipAddress, port);
    }

    @Override
    public void clearRegistry() {
        NamingService nacosClient = NacosUtil.getNacosClient();
        InetSocketAddress address = new InetSocketAddress(RPC_SERVER_ADDRESS, RPC_SERVER_PORT);
        NacosUtil.clearNacosRegister(nacosClient, address);
    }
}
