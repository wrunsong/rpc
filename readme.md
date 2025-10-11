**简单的RPC框架，用于学习和面试**
<br><br>
使用方式: <br>
1. 环境要求：本地或者线上环境有zookeeper，在Constants类对zk配置；<br>
2. 先启动NettyServer的main方法启动服务端；<br>
3. 再启动NettyClient的main方法启动客户端；<br>
4. 控制台会打印调用的rpc方法
<br>

TODO List:
1. 钩子没发挥作用  ✅
2. client和server 与spring集成
3. 熔断
4. 降级
5. nacos
6. kafka
7. 负载均衡
8. 集群
9. 用netty的零拷贝，如transferTo等方法
10. 用cglib做代理
11. 用yaml配置，不用Constans配置