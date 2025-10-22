package lilac.rpcframework.config.yaml.field;

import lilac.rpcframework.factory.SingletonFactory;

public class RegistryYamlConfig {
    private String type;
    private String address;
    private int maxRetries;
    private ZkYamlConfig zk;
    private NacosYamlConfig nacos;

    void initialize() {



        if (type == null || type.isEmpty()) {
            type = "zk";  // 默认注册类型
        }
        if (address == null || address.isEmpty()) {
            if ("zk".equalsIgnoreCase(type)) {
                address = "localhost:2181";  // 默认 ZK 地址
            } else if ("nacos".equalsIgnoreCase(type)) {
                address = "localhost:8848";  // 默认 Nacos 地址
            }
        }

        if (maxRetries <= 0) {
            maxRetries = 3;  // 默认最大重试次数
        }

        if (this.type.equals("zk")) {
            if (zk != null) {
                zk.initialize();
            } else {
                zk = SingletonFactory.getInstance(ZkYamlConfig.class);
                if (zk != null) {
                    zk.initialize();
                } else {
                    System.err.println("zk is null");
                }
            }
        }  else if (this.type.equals("nacos")) {
            if (nacos != null) {
                nacos.initialize();
            } else  {
                nacos = SingletonFactory.getInstance(NacosYamlConfig.class);
                if (nacos != null) {
                    nacos.initialize();
                } else  {
                    System.err.println("nacos is null");
                }
            }
        } else {
            System.err.println("type is null");
        }

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public ZkYamlConfig getZk() {
        return zk;
    }

    public void setZk(ZkYamlConfig zk) {
        this.zk = zk;
    }

    public NacosYamlConfig getNacos() {
        return nacos;
    }

    public void setNacos(NacosYamlConfig nacos) {
        this.nacos = nacos;
    }
}
