package lilac.rpcframework.spring;

import lilac.rpcframework.annotations.RpcScan;
import lilac.rpcframework.config.yaml.LoadRpcFrameworkYamlConfig;
import lilac.rpcframework.config.yaml.field.TopYamlConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * 将@RpcScan注解修饰的类交给Spring管理
 */

@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final TopYamlConfig yamlConfig = LoadRpcFrameworkYamlConfig.loadFromYaml();


    private static final String SPRING_BEAN_BASE_PACKAGE = yamlConfig.getLilacRpc().getSpringBeanBasePackage();

    private static ResourceLoader resourceLoader;

    private static final String RPC_SCAN_ATTRIBUTE_NAME = "scanPackages";

    // TODO 什么用处？
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        CustomScannerRegistrar.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));

        String[] rpcScanPackages = new String[0];
        if (annotationAttributes != null) {
            rpcScanPackages = annotationAttributes.getStringArray(RPC_SCAN_ATTRIBUTE_NAME);
        }
        // scan class with @RpcScan
        CustomScanner rpcScanner = new CustomScanner(beanDefinitionRegistry, RpcScan.class);
        // scan class with @Component
        CustomScanner componentScanner = new CustomScanner(beanDefinitionRegistry, Component.class);

        if (resourceLoader != null) {
            rpcScanner.setResourceLoader(resourceLoader);
            componentScanner.setResourceLoader(resourceLoader);
        }

        int componentBeanAmount = componentScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("componentBeanAmount = {}", componentBeanAmount);
        int rpcBeanAmount = rpcScanner.scan(rpcScanPackages);
        log.info("rpcBeanAmount = {}", rpcBeanAmount);
    }
}
