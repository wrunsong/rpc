package lilac.rpcframework.config.yaml.field;

public class CodecYamlConfig {
    private String type;

    void initialize() {
        if (type == null || type.isEmpty()) {
            type = "hessian";  // 默认负载均衡算法
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
