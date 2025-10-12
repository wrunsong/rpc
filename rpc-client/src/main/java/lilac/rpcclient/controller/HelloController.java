package lilac.rpcclient.controller;

import lilac.api.service.HelloService;
import lilac.api.dto.HelloDto;
import lilac.rpcframework.annotations.RpcReference;
import lilac.api.utils.RpcTestUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rpc/client")
public class HelloController {
    @RpcReference(version = "v1", group = "lilac")
    private HelloService helloService;

    @RpcReference()
    private RpcTestUtil rpcTestUtil;


    @GetMapping("/say/hello")
    public String sayHello() {

        String hello = helloService.hello(new HelloDto("message", "description"));
        System.out.println(hello + "\n");

        rpcTestUtil.test();

        return hello;

    }

}
