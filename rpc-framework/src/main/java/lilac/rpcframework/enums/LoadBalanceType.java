package lilac.rpcframework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LoadBalanceType {
    // TODO 改成具体的负载均衡名称
    CONSISTENT_HASH("consistentHash"),;

    private final String name;
}
