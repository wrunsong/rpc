package lilac.rpcframework.remote.handler;

import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.provider.ServiceProvider;
import lilac.rpcframework.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
public class RpcRequestHandler {

    private final ServiceProvider serviceProvider;

    @Value("${lilac.rpc.registry.type:zookeeper}")
    private static String registryType;

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
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
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
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            method.setAccessible(true);
            result = method.invoke(service, rpcRequest.getParameters());
        } catch (Exception e) {
            log.error("Call remote method {} failed: {}", rpcRequest.getMethodName(), e.getMessage());
        }
        return result;
    }


}
