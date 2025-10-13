**简单的RPC框架，用于学习和面试**
<br><br>
使用方式: <br>
1. 环境要求：本地或者线上环境有zookeeper，在Constants类对zk配置；<br>
2. 先启动RpcServerApplication以启动服务端；<br>
3. 再启动RpcClientApplication启动客户端；<br>
4. 控制台会打印调用的rpc方法
<br>

TODO List:
1. 钩子没发挥作用  ✅
2. client和server 与spring集成 ✅
3. 熔断
4. 降级
5. nacos
6. kafka
7. 负载均衡
8. 集群
9. 用netty的零拷贝，如transferTo等方法
10. 用cglib做代理 ✅
11. 用yaml配置，不用Constants配置
12. 加载yaml配置的时候，用的静态方法，先于spring，slf4j好像没法用
13. protobuf序列化方式

技术点<br>
1. 服务端注册了一个zk的钩子程序，在服务端结束时(@PreDestroy)会自动清除zk上自己的IP地址；
2. 服务端只要启动springboot服务，就能自动启动rpc服务器(RpcServerAutoStarter)、客户端只要启动springboot服务，需要由rpc调用的接口就自动被代理bean替换(SpringBeanPostProcess)；
3. 暴露给客户端的api既可以是接口类型的（用jdk代理发起rpc），也可以是类类型的（用cglib代理发起rpc）;
4. 可以在@RpcService指定服务端要暴露的服务在注册中心的名字，可以在@RpcReference指定客户端要查找的服务在注册中心的名字；
5. 用jackson读取自定义的rpc.yaml文件进行配置