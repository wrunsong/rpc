package lilac.rpcclient;


import lilac.rpcclient.controller.HelloController;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

//@RpcScan(scanPackages = {"lilac"})
public class NettyClient {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClient.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        String s = helloController.sayHello();
        System.out.println(s);
    }

}
