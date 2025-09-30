package lilac.rpcframework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RpcPattern {

    NETTY("netty"),
    SOCKET("socket");

    private final String pattern;
}
