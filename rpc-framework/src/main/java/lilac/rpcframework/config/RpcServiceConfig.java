package lilac.rpcframework.config;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RpcServiceConfig {

    private String group;
    private String version;
    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + ":"
                + "group:" +  this.getGroup()
                + ",version:" + this.getVersion();
    }

    private String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }

}
