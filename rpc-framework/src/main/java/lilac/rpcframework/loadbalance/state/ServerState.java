package lilac.rpcframework.loadbalance.state;

import lilac.rpcframework.loadbalance.state.instance.LeastConnectionsInstance;
import lilac.rpcframework.loadbalance.state.instance.LeastTimeInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用来标记服务端的状态
 */
public class ServerState {

    // IP:port -> number of server connections
    public static final Map<String, LeastConnectionsInstance> SERVER_CONNECTION_COUNTER = new ConcurrentHashMap<>();

    // IP:port -> average response time
    public static final Map<String, LeastTimeInstance> SERVER_RESPONSE_TIME = new ConcurrentHashMap<>();
}
