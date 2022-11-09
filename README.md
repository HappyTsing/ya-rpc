# Yet Another RPC Framework

本项目首先讲解 RPC 的发展史，即为什么要这么实现 RPC？

随后使用 Zookeeper + Netty + Protobuf 实现一个RPC框架。

最后给出框架 Demo，其实现下述任务：

1. 编写一个简易RPC框架，YA-RPC
2. 支持基本数据类型：int, float, string
3. 支持 At-least-once 语义
4. 使用YA-RPC编写一个demo程序，实现如下API：
    1. 远程调用 float sum(float a, float b)
    2. 远程调用 string uppercase(str)
    3. 不少于2个客户端，1个服务端
