package lilac.rpcframework.config.yaml.listener;

import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplicationRunListener;

public class RpcFrameworkRunListener implements SpringApplicationRunListener {
    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        // 将自定义的yaml文件导入
        TopYamlConfig topYamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
        SpringApplicationRunListener.super.starting(bootstrapContext);
    }
}
