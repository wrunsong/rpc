package lilac.server.impl;

import lilac.rpcframework.annotations.RpcService;
import org.springframework.stereotype.Service;

@Service
@RpcService(exposeName = "RpcTestUtil")
public class RpcTestUtilImpl {

    public String test() {
        String str = "use proxy!";
        System.out.println(str);
        return str;
    }
}
