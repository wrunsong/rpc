package lilac.rpcframework.config.yaml.field;

public class ZkYamlConfig {
    private String path;

    void initialize() {
        if (path == null || path.isEmpty()) {
            path = "/lilac-rpc";  // 默认 zk 路径
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
