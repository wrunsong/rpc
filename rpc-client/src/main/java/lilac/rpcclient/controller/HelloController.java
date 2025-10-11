package lilac.rpcclient.controller;

import lilac.HelloService;
import lilac.dto.HelloDto;
import lilac.rpcframework.annotations.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rpc/client")
public class HelloController {
    @RpcReference(version = "v1", group = "lilac")
    private HelloService helloService;


    @GetMapping("/say/hello")
    public String sayHello() {

        String hello = helloService.hello(new HelloDto("message", "description"));
        System.out.println(hello);
        return hello;

    }

}
