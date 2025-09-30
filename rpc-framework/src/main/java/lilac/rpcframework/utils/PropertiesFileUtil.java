package lilac.rpcframework.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
public class PropertiesFileUtil {

    private PropertiesFileUtil() {}

    public static Properties readPropertiesFile(String fileName) {
        // TODO 这个url是什么
        URL url = PropertiesFileUtil.class.getClassLoader().getResource("");
        String rpcConfigPath = "";

        if (url != null) {
            rpcConfigPath = url.getPath() + fileName;
        }

        Properties properties = null;
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(reader);
        } catch (Exception e) {
            log.error("Failed to read properties file {}. Error message: {}", fileName, e.getMessage());
        }
        return properties;
    }
}
