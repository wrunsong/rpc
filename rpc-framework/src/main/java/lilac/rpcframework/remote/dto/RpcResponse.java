package lilac.rpcframework.remote.dto;

import lilac.rpcframework.enums.RpcResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 715745410605631233L;
    private String requestId;
    /**
     * response code
     */
    private Integer code;
    /**
     * response message
     */
    private String message;
    /**
     * response body
     */
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> rpcResponse = new RpcResponse<>();
        rpcResponse.setCode(RpcResponseCode.SUCCESS.getCode());
        rpcResponse.setMessage(RpcResponseCode.SUCCESS.getMessage());
        rpcResponse.setRequestId(requestId);
        if (data != null) {
            rpcResponse.setData(data);
        }
        return rpcResponse;
    }

    public static <T> RpcResponse<T> fail() {
        RpcResponse<T> rpcResponse = new RpcResponse<>();
        rpcResponse.setCode(RpcResponseCode.FAIL.getCode());
        rpcResponse.setMessage(RpcResponseCode.FAIL.getMessage());
        return rpcResponse;
    }
}
