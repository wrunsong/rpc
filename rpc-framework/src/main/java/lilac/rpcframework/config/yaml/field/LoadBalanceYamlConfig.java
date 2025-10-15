package lilac.rpcframework.config.yaml.field;

public class LoadBalanceYamlConfig {
    private String type;

    void initialize() {
        if (type == null || type.isEmpty()) {
            type = "consistentHash";  // 默认序列化方式
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
