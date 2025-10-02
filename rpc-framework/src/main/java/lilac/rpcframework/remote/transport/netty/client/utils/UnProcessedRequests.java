package lilac.rpcframework.remote.transport.netty.client.utils;

import lilac.rpcframework.remote.dto.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 未处理请求的缓存
 */
@Slf4j
public class UnProcessedRequests {

    // requestId -> response
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSES = new ConcurrentHashMap<>();

    private UnProcessedRequests() {}

    public static void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_RESPONSES.put(requestId, future);
    }

    public  static CompletableFuture<RpcResponse<Object>> remove(String requestId) {
        return UNPROCESSED_RESPONSES.remove(requestId);
    }

    public static void complete(RpcResponse<Object> response) {
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSES.get(response.getRequestId());
        if (future == null) {
            log.error("Don't get response for requestId= {}", response.getRequestId());
            return;
        }
        future.complete(response);
    }


}
