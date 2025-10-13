package lilac.rpcframework.config.yaml.field;

import lilac.rpcframework.factory.SingletonFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopYamlConfig {
    private RpcYamlConfig lilacRpc;

    private TopYamlConfig() {}

    public void initialize() {
        this.lilacRpc = SingletonFactory.getInstance(RpcYamlConfig.class);
        if (lilacRpc != null) {
            lilacRpc.initialize();
        } else {
            log.error("rpc config is null");
        }
    }

    public RpcYamlConfig getLilacRpc() {
        return lilacRpc;
    }

    public void setLilacRpc(RpcYamlConfig lilacRpc) {
        this.lilacRpc = lilacRpc;
    }
}
