package lilac.rpcframework.config.yaml.field;

public class NacosYamlConfig {

    private String namespace;

    private int configRetryTime;

    private String username;

    private String password;

    void initialize(){
        if (namespace == null || namespace.isEmpty()) {
            namespace = "public";
        }

        if (configRetryTime <= 0) {
            configRetryTime = 5000;
        }

        if (username == null || username.isEmpty()) {
            username = "nacos";
        }
        if (password == null || password.isEmpty()) {
            password = "nacos";
        }
    }

    public int getConfigRetryTime() {
        return configRetryTime;
    }

    public void setConfigRetryTime(int configRetryTime) {
        this.configRetryTime = configRetryTime;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
