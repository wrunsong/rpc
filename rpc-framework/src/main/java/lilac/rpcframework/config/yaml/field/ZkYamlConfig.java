package lilac.rpcframework.config.yaml.field;

public class ZkYamlConfig {
    private String path;

    private int baseSleepTime;

    void initialize() {
        if (path == null || path.isEmpty()) {
            path = "/lilac-rpc";  // 默认 zk 路径
        }
        if (baseSleepTime <= 0) {
            baseSleepTime = 3000;
        }
    }

    public int getBaseSleepTime() {
        return baseSleepTime;
    }

    public void setBaseSleepTime(int baseSleepTime) {
        this.baseSleepTime = baseSleepTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
