package lilac.rpcframework.config.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;

public class LoadRpcFrameworkYamlConfig {

    private static final Logger log = LoggerFactory.getLogger(LoadRpcFrameworkYamlConfig.class);
    private static volatile TopYamlConfig lilac = null;
    // TODO 换成classLoader
    private static final String filePath = "rpc-framework/src/main/resources/lilac-rpc.yaml";

    private LoadRpcFrameworkYamlConfig() {}

    // 加载 YAML 配置文件
    public static TopYamlConfig loadFromYaml() {

        if (lilac == null) {
            synchronized (LoadRpcFrameworkYamlConfig.class) {
                if (lilac == null) {
                    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
                    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

                    try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
                        lilac = objectMapper.readValue(fileInputStream, TopYamlConfig.class);
                        lilac.initialize();
                        return lilac;
                    } catch (Exception e) {
                        // spring还没起来，log不起作用
                        System.err.println("loadFromYaml error: " + e.getMessage());
                        // 抛异常就返回默认初始化配置
                        lilac.initialize();
                        return lilac;
                    }

                }
            }
        }
        return lilac;
    }




}
