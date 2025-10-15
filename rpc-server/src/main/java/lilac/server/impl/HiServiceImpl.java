package lilac.server.impl;

import lilac.api.service.HelloService;
import lilac.rpcframework.api.dto.HelloDto;
import lilac.rpcframework.annotations.RpcService;
import org.springframework.stereotype.Service;

@Service
@RpcService(version = "v2", group = "lilac")
public class HiServiceImpl implements HelloService {
    @Override
    public String hello(HelloDto helloDto) {
        return "Server get the client request, and server use *HelloServiceImpl* process, " +
                "The message from client is: " + helloDto.getMessage() +
                ", and the description from client is: " + helloDto.getDescription() +
                ", and the server answer is ***rpc impl_2 success***";
    }
}
