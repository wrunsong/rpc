package lilac.rpcframework.config.yaml.field;

import lilac.rpcframework.factory.SingletonFactory;

public class RpcYamlConfig {
    private String serverAddress;
    private int serverPort;
    private int maxIdleTime;
    private String springBeanBasePackage;
    private RegistryYamlConfig registry;
    private CompressYamlConfig compress;
    private CodecYamlConfig codec;
    private LoadBalanceYamlConfig loadbalance;

    void initialize() {

        if (serverAddress == null || serverAddress.isEmpty()) {
            serverAddress = "127.0.0.1";  // 默认地址
        }
        if (serverPort <= 0) {
            serverPort = 1018;  // 默认端口
        }
        if (maxIdleTime <= 0) {
            maxIdleTime = 1;  // 默认最大空闲时间
        }

        if (springBeanBasePackage == null || springBeanBasePackage.isEmpty()) {
            springBeanBasePackage = "lilac";  // 默认包名
        }

        if (registry != null) {
            registry.initialize();
        } else {
            registry = SingletonFactory.getInstance(RegistryYamlConfig.class);
            if (registry != null) {
                registry.initialize();
            }
        }

        if (compress != null) {
            compress.initialize();
        } else {
            compress = SingletonFactory.getInstance(CompressYamlConfig.class);
            if (compress != null) {
                compress.initialize();
            }
        }

        if (codec != null) {
            codec.initialize();
        } else {
            codec = SingletonFactory.getInstance(CodecYamlConfig.class);
            if (codec != null) {
                codec.initialize();
            }
        }


        if (loadbalance != null) {
            loadbalance.initialize();
        } else {
            loadbalance = SingletonFactory.getInstance(LoadBalanceYamlConfig.class);
            if (loadbalance != null) {
                loadbalance.initialize();
            }
        }

        if (registry == null || compress == null || codec == null || loadbalance == null) {
            System.err.println("registry or compress or codec or loadbalance is null");
            return;
        }
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public String getSpringBeanBasePackage() {
        return springBeanBasePackage;
    }

    public void setSpringBeanBasePackage(String springBeanBasePackage) {
        this.springBeanBasePackage = springBeanBasePackage;
    }

    public RegistryYamlConfig getRegistry() {
        return registry;
    }

    public void setRegistry(RegistryYamlConfig registry) {
        this.registry = registry;
    }

    public CompressYamlConfig getCompress() {
        return compress;
    }

    public void setCompress(CompressYamlConfig compress) {
        this.compress = compress;
    }

    public CodecYamlConfig getCodec() {
        return codec;
    }

    public void setCodec(CodecYamlConfig codec) {
        this.codec = codec;
    }

    public LoadBalanceYamlConfig getLoadbalance() {
        return loadbalance;
    }

    public void setLoadbalance(LoadBalanceYamlConfig loadbalance) {
        this.loadbalance = loadbalance;
    }
}
