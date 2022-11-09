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

# Question

服务提供者和服务消费者如何约定接口？

通常是在一个单独的模块中定义接口，服务提供者和服务消费者都通过Maven依赖来引用此模块。本文档为了简便，服务提供者和服务消费者分别创建两个完全一模一样的接口，实际使用中不推荐这样使用。

> Reference: 
> - https://help.aliyun.com/document_detail/97471.html
> - https://juejin.cn/post/6844903950974468104 参考这个组织demo的文件架构