package lilac.rpcframework.remote.handler;

import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.provider.ServiceProvider;
import lilac.rpcframework.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;

    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();
    private static final String registryType = yamlConfig.getLilacRpc().getRegistry().getType();

    public RpcRequestHandler() {
        this.serviceProvider = Objects.requireNonNull(
                ExtensionLoader.getExtensionLoader(ServiceProvider.class)).getExtension(registryType);
    }

    /**
     * 从ServiceProvider中获取方法，并返回方法的处理结果
     * @param rpcRequest
     * @return
     */
    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.getFullyExposeName());
        return invoke(rpcRequest, service);
    }

    /**
     * 执行客户端调用的方法
     * @param rpcRequest
     * @param service
     * @return
     */
    private Object invoke(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), castStringToClass(rpcRequest.getParamTypes()));
            method.setAccessible(true);
            result = method.invoke(service, rpcRequest.getParameters());
        } catch (Exception e) {
            log.error("Call remote method {} failed: {}", rpcRequest.getMethodName(), e.getMessage());
        }
        return result;
    }

    private Class<?>[] castStringToClass(String[] paramTypes) {
        Class<?>[] classes = new Class<?>[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            try {
                classes[i] = Class.forName(paramTypes[i]);
            } catch (ClassNotFoundException e) {
                log.error("Cast paramTypes error: {}", e.getMessage());
            }
        }
        return classes;
    }


}
