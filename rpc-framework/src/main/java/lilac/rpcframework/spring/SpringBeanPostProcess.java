package lilac.rpcframework.spring;

import lilac.rpcframework.annotations.RpcReference;
import lilac.rpcframework.annotations.RpcService;
import lilac.rpcframework.config.RpcServiceConfig;
import lilac.rpcframework.constants.Constants;
import lilac.rpcframework.extension.ExtensionLoader;
import lilac.rpcframework.factory.SingletonFactory;
import lilac.rpcframework.provider.ServiceProvider;
import lilac.rpcframework.proxy.RpcClientProxy;
import lilac.rpcframework.remote.transport.netty.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Objects;

@Slf4j
@Component
public class SpringBeanPostProcess implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final NettyRpcClient nettyRpcClient;

    private static final String registryType = Constants.REGISTRY_TYPE;
    private static final String proxyType = Constants.PROXY_TYPE;

    public SpringBeanPostProcess() {
        this.serviceProvider = Objects.requireNonNull(
                ExtensionLoader.getExtensionLoader(ServiceProvider.class)).getExtension(registryType);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * spring初始化前 -> 将服务发布到注册中心
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {

            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);

            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean)
                    .build();

            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    /**
     * spring初始化后 -> 生成代理对象
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            boolean present = field.isAnnotationPresent(RpcReference.class);
            if (present) {
                RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version())
                        .build();

                RpcClientProxy rpcClientProxy = Objects.requireNonNull(
                        ExtensionLoader.getExtensionLoader(RpcClientProxy.class)).getExtension(proxyType);

                rpcClientProxy.setClient(nettyRpcClient, rpcServiceConfig);

                Object proxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                try {
                    // 用proxy类代替bean中字段的原始类
                    field.set(bean, proxy);
                } catch (IllegalAccessException e) {
                    log.error("init rpc proxy error: bean name = {}, error message = {}", beanName, e.getMessage());
                }
            }
        }
        return bean;
    }
}
