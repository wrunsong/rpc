package lilac.rpcframework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServiceRegistryType {

    ZK("zookeeper"),
    NACOS("nacos");

    private final String type;
}
