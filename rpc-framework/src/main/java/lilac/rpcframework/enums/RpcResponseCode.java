package lilac.rpcframework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RpcResponseCode {

    SUCCESS(200, "The RPC call is success."),
    FAIL(500, "The RPC call is fail.");

    private final int code;
    private final String message;
}
