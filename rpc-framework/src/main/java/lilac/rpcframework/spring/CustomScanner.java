package lilac.rpcframework.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

//扫描指定包路径下带有指定注解的类，然后将它们注册为 Spring 的 Bean。这样可以实现只扫描特定注解标记的类，而不是默认的如 @Component 等。
public class CustomScanner extends ClassPathBeanDefinitionScanner {

    public CustomScanner(BeanDefinitionRegistry registry, Class<? extends Annotation> annotation) {
        super(registry);
        super.addIncludeFilter(new AnnotationTypeFilter(annotation));
    }

    @Override
    public int scan(String... scanPackages) {
        return super.scan(scanPackages);
    }
}
