package lilac.rpcframework.config.yaml.field;

public class CompressYamlConfig {
    private String type;
    private int bufferSize;

    void initialize() {
        if (type == null || type.isEmpty()) {
            type = "gzip";  // 默认压缩类型
        }
        if (bufferSize <= 0) {
            bufferSize = 4096;  // 默认缓冲区大小
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
