package lilac.rpcframework.constants;

@Deprecated
public class Constants {

    public static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    public static final int BASE_SLEEP_TIME = 3000;
    public static final int MAX_RETRIES = 3;
    public static final String ZOOKEEPER_ROOT_PATH = "/lilac-rpc";

    public static final String SPRING_BEAN_BASE_PACKAGE = "lilac";

    public static final int BUFFER_SIZE = 4096;

    public static final String REGISTRY_TYPE = "zk";

    public static final int SERVER_PORT = 8080;

    public static final int MAX_IDLE_TIMES = 1;

    public static final String CODEC_TYPE = "hessian";

    public static final String COMPRESS_TYPE = "gzip";

    public static final String LOAD_BALANCE_TYPE = "consistentHash";

}
