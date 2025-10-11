package lilac.rpcclient.controller;

import lilac.HelloService;
import lilac.dto.HelloDto;
import lilac.rpcframework.annotations.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class HelloController {
    @RpcReference(version = "v1", group = "lilac")
    private HelloService helloService;

    public String sayHello() {

        String hello = helloService.hello(new HelloDto("message", "description"));
        System.out.println(hello);
        return hello;

    }

}
