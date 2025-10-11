package lilac.rpcframework.registry.zk;

import lilac.rpcframework.constants.Constants;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.loadbalance.LoadBalance;
import lilac.rpcframework.registry.ServiceRegistry;
import lilac.rpcframework.registry.zk.util.CuratorUtil;
import lilac.rpcframework.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {

    private final LoadBalance loadBalance;
    private int SERVER_PORT = Constants.SERVER_PORT;


    private static final String loadBalanceType = Constants.LOAD_BALANCE_TYPE;

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
    public InetSocketAddress lookupService(RpcRequest request) {
        String rpcServiceName = request.getRpcServiceName();

        CuratorFramework zkClient = CuratorUtil.getZkClient();
        List<String> addresses = CuratorUtil.getChildrenNodes(zkClient, rpcServiceName);
        if (addresses == null || addresses.isEmpty()) {
            log.error("no server can provide service: {}",  rpcServiceName);
        }
        String targetAddress = loadBalance.selectServiceAddress(addresses, request);
        String[] strings = targetAddress.split(":");
        String ipAddress = strings[0];
        int port = Integer.parseInt(strings[1]);
        return new InetSocketAddress(ipAddress, port);
    }

    @Override
    public void clearRegistry() {
        try {
            CuratorFramework zkClient = CuratorUtil.getZkClient();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), SERVER_PORT);
            CuratorUtil.clearZkRegistry(zkClient, inetSocketAddress);
        } catch (Exception e) {
            log.error("clear registry error: {}", e.getMessage());
        }
    }
}
