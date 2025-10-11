package lilac.server.impl;

import lilac.HelloService;
import lilac.dto.HelloDto;
import lilac.rpcframework.annotations.RpcService;
import org.springframework.stereotype.Service;

@Service
@RpcService(version = "v1", group = "lilac")
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(HelloDto helloDto) {

        return "Server get the client request, and server use *HelloServiceImpl* process, " +
                "The message from client is: " + helloDto.getMessage() +
                ", and the description from client is: " + helloDto.getDescription() +
                ", and the server answer is ***rpc impl_1 success***";
    }
}
