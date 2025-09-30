package lilac.rpcframework.annotations;

import java.lang.annotation.*;

/**
 * 该注解用于修饰提供的RPC服务
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface RpcService {
    String version() default "v1";

    String group() default "lilac";
}
