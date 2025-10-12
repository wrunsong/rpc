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
    private String fullyExposeName;

    public void setFullyExposeName(String serviceName) {
        this.fullyExposeName = serviceName + ":"
                + "group:" +  this.getGroup()
                + ",version:" + this.getVersion();
    }



}
