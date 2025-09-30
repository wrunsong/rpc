package lilac.rpcframework.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServiceRegistryType {

    REGISTRY("registry"),
    NACOS("nacos");

    private final String type;
}
