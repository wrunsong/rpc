package lilac.rpcframework.annotations;

import lilac.rpcframework.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于修饰需要在Spring启动时特殊处理的类
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
public @interface RpcScan {
    String[] scanPackages() default {};
}
