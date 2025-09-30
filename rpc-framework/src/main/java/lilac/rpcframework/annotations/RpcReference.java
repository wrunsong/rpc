package lilac.rpcframework.annotations;

import java.lang.annotation.*;

/**
 * 该注解用于客户端，用于标记用户想要调用的服务的版本、发布组织等信息
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface RpcReference {
    String version() default "v1";

    String group() default "lilac";
}
