package lilac.rpcframework.registry.zk;

import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.loadbalance.LoadBalance;
import lilac.rpcframework.registry.ServiceRegistry;
import lilac.rpcframework.registry.zk.util.CuratorUtil;
import lilac.rpcframework.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {

    private final LoadBalance loadBalance;

    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private static final String RPC_SERVER_ADDRESS = yamlConfig.getLilacRpc().getServerAddress();
    private static final int RPC_SERVER_PORT = yamlConfig.getLilacRpc().getServerPort();
    private static final String loadBalanceType = yamlConfig.getLilacRpc().getLoadbalance().getType();

    public ZkServiceRegistry() {
        this.loadBalance = Objects.requireNonNull(ExtensionLoader.getExtensionLoader(LoadBalance.class))
                .getExtension(loadBalanceType);
    }

    /**
     *  服务注册，创建zk结点
     * @param rpcServiceName 服务名称 methodName + group + version
     * @param inetSocketAddress IP
     */
    @Override
    public void register(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        CuratorUtil.createPersistentNode(zkClient,  rpcServiceName + inetSocketAddress.toString());
    }


    /**
     * 服务发现，找到能够提供服务的合适服务端IP
     * @param request 客户端发起的请求
     * @return
     */
    @Override
    public InetSocketAddress lookupService(RpcRequest request, String clientAddress) {
        String rpcServiceName = request.getFullyExposeName();

        CuratorFramework zkClient = CuratorUtil.getZkClient();
        List<String> addresses = CuratorUtil.getChildrenNodes(zkClient, rpcServiceName);
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
        try {
            CuratorFramework zkClient = CuratorUtil.getZkClient();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(RPC_SERVER_ADDRESS, RPC_SERVER_PORT);
            CuratorUtil.clearZkRegistry(zkClient, inetSocketAddress);
        } catch (Exception e) {
            log.error("clear registry error: {}", e.getMessage());
        }
    }
}
