package lilac.api.service;


import lilac.rpcframework.api.dto.HelloDto;

public interface HelloService {

    String hello(HelloDto helloDto);
}
