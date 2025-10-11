package lilac.rpcframework.remote.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
@ToString
@NoArgsConstructor
public class RpcRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1905122041950251207L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    // version 字段（服务版本）主要是为后续不兼容升级提供可能。
    private String version;
    // group 字段主要用于处理一个接口有多个类实现的情况。
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName() + ":"
                + "group:" +  this.getGroup()
                + ",version:" + this.getVersion();
    }


}
