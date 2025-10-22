# **简单的RPC框架，用于学习和面试**
<br><br>
## 使用方式: <br>
1. 环境要求：本地或者线上环境有zookeeper，在lilac-rpc.yaml对rpc-framework进行配置；<br>
2. 先启动RpcServerApplication以启动服务端；<br>
3. 再启动RpcClientApplication启动客户端；<br>
4. 控制台会打印调用的rpc方法
<br>

## TODO List:
1. 钩子没发挥作用  ✅
2. client和server 与spring集成 ✅
3. 熔断
4. 降级
5. nacos ✅
6. kafka
7. 负载均衡 ✅
8. 集群
9. 用netty的零拷贝，如transferTo等方法
10. 用cglib做代理 ✅
11. 用yaml配置，不用Constants配置 ✅
12. 加载yaml配置的时候，用的静态方法，先于spring，slf4j好像没法用 ✅
13. protobuf序列化方式 ✅
14. SPI机制是否真的可以在client/server模块下进行扩展？✅--- 可以, 因为是用的ClassLoader加载的资源，
而rpc-framework、client和server都是由Application ClassLoader加载的，所以不管extensions目录在哪个resource下，都能被找到
15. 使用户的extensions配置能够追加框架原有的 ✅
16. caffe做缓存

## 技术点<br>
1. 服务端注册了一个zk的钩子程序，在服务端结束时(@PreDestroy)会自动清除zk上自己的IP地址；
2. 服务端只要启动springboot服务，就能自动启动rpc服务器(RpcServerAutoStarter)、客户端只要启动springboot服务，需要由rpc调用的接口就自动被代理bean替换(SpringBeanPostProcess)；
3. 暴露给客户端的api既可以是接口类型的（用jdk代理发起rpc），也可以是类类型的（用cglib代理发起rpc）;
4. 可以在@RpcService指定服务端要暴露的服务在注册中心的名字，可以在@RpcReference指定客户端要查找的服务在注册中心的名字；
5. 用jackson读取自定义的lilac-rpc.yaml文件对rpc进行配置;
6. 支持protobuf和hessian两种序列化方式；
7. 用户可以在resources/extensions中以同样的方式追加SPI配置;
8. 增加了轮询、IP哈希、最小连接数（pick-2）、最小时延（pick-2）的负载均衡方式；
9. 新增了nacos作为服务发现和注册的平台