# Yet Another RPC Framework

参考 `dubbo` 的简易 RPC 框架，

前序知识请查看：[The Simple Tutorial Of RPC](The%20Simple%20Tutorial%20Of%20RPC.md)

## 快速开始

### 1. 启动注册中心：zookeeper 集群

```shell
cd ya-rpc-demo/registry
sh init_zookeeper_cluster.sh
```

### 2. 实现服务端：Server

参考 `/ya-rpc-demo/ya-rpc-server`。

首先 `pom.xml` 中引入依赖模块:

- `ya-rpc-api`：客户端和服务端约定的服务接口
- `ya-rpc-core`：ya-rpc 框架

其次，在服务端实现 `ya-rpc-api` 模块约定的接口，每个接口可以提供多种实现，使用 `group` 和 `version` 进行唯一标记：

- group：一个接口有多个实现类的情况，需要对服务进行分组
- version：通常在接口不兼容时版本号才需要升级。为什么要增加服务版本号？为后续不兼容升级提供可能，比如服务接口增加方法，或服务模型增加字段，可向后兼容，删除方法或删除字段，将不兼容，枚举类型新增字段也不兼容，需通过变更版本号升级。

随后，Server 注册提供的服务，构造服务签名时，可以传入 `service.class`，也可以直接传入服务实例 `new service()`

最后使用 `start()` 方法启动服务，将监听 Client 的连接，并为每次请求分配线程处理。

```java
public class SocketServerMain {
    public static void main(String[] args) {
        SocketRpcServer server = new SocketRpcServer();
        server.registerService(new ServiceSignature(UserServiceImpl.class,"groupName1","version1"), UserServiceImpl.class);
        server.registerService(new ServiceSignature(new UtilServiceImpl(),"groupName1","version1"), UtilServiceImpl.class);
        server.start();
    }
}
```

### 3. 实现客户端：Client

参考 `/ya-rpc-demo/ya-rpc-server`。

同样，首先 `pom.xml` 中引入依赖模块:

- `ya-rpc-api`：客户端和服务端约定的服务接口
- `ya-rpc-core`：ya-rpc 框架

一般来说，客户端就是使用 RPC 服务调用接口，在业务代码中使用即可。

```java
public class SocketClientMain {
    public static void main(String[] args) {
        SocketRpcClient client = new SocketRpcClient();
        UtilService utilService = client.getStub(UtilService.class,"groupName1","version1");
        double sum = utilService.sum(22.08,06.26);
        String uppercase = utilService.uppercase("happytsing");
        log.info("sum: {} uppercase: {}",sum,uppercase);
    }
}
```

## RPC 模块介绍

```
.
├── ya-rpc-common     【module1: 基础代码】
│    ├── config           用户自定义配置
│    ├── consts           常量，主要是协议常量
│    ├── dto              数据传输对象
│    ├── enums            枚举类
│    ├── exception        异常类
│    ├── extension        增强版 SPI，简化的 dubbo 版本
│    └── threadpool       线程池
├── ya-rpc-core       【module2: 核心代码】
│    ├── serialize        序列化，提供了 protostuff、hessian 实现
│    ├── compress         压缩，提供了 gzip 和 dummy（空实现）
│    ├── loadbalance      负载均衡，提供了随机和轮询策略
│    ├── registry         注册中心，提供了 zookeeper 实现
│    └── remoting         远程传输，网络协议相关的内容
├── ya-rpc-demo       【module3: 实例代码】
│    ├── registry         注册中心集群的初始化
│    ├── ya-rpc-api       服务接口
│    ├── ya-rpc-client    客户端
│    └── ya-rpc-server    服务端
└── ya-rpc-evolution  【moudle4: RPC 架构的演化示例代码】
```

其中 `remoting` 中是核心中的核心，略作介绍：

```
remoting
├── RequestHandler.java
├── RpcClient.java
├── RpcServer.java
├── netty
│    ├── client
│    ├── codec
│    └── server
└── socket
     ├── client
     │    └── SocketRpcClient.java                     Client 类
     ├── codec
     │    └── SocketRpcMessageCodec.java               编码器
     └── server
          ├── ServerHook.java                          钩子函数
          ├── ServiceRegedit.java                      注册表
          ├── SocketRpcServer.java                     Server 类
          └── SocketRpcServerRequestHandler.java       请求处理线程
```

- 编码器：主要是编码协议和解码自定义的协议。
- 钩子函数：一个用于在关机时取消本机注册的服务，另一个用于删除服务端实现 `At-Most-Once` 语义的缓存。
- 注册表：Server 向注册中心注册服务时，会更新该表，添加一条记录。
  Client 向 Server 请求服务时，Server 根据请求的数据，从注册表中拿出服务接口的实现类.class，并基于此构建实现类实例，执行对应的方法。
- Client 的每次请求，Server 都为其分配一个线程处理请求。

## 协议

`ya-rpc` 框架的自定义协议如下：

![ya-rpc-protocol](https://happytsing-figure-bed.oss-cn-hangzhou.aliyuncs.com/rpc/ya-rpc-protocol.png)

- 4B magic code（魔数）：通信双方协商的一个暗号。魔数的作用是用于服务端在接收数据时先解析出魔数做正确性对比。如果和协议中的魔数不匹配，则认为是非法数据，可以直接关闭连接或采取其他措施增强系统安全性。

  > **注意**：这只是一个简单的校验，如果有安全性方面的需求，需要使用其他手段，例如 SSL/TLS。
  >
  > 魔数的思想在很多场景中都有体现，如 Java Class 文件开头就存储了魔数 OxCAFEBABE，在 JVM 加载 Class 文件时首先就会验证魔数对的正确性。

- 1B version（版本）：为了应对业务需求的变化，可能需要对自定义协议的结构或字段进行改动。不同版本的协议对应的解析方法也是不同的。所以在生产级项目中强烈建议预留协议版本这个字段。

- 1B message type（消息类型）：消息类型包括，普通请求、普通响应。解码器可以根据消息类型来确定解析的类型。

- 1B compressor（压缩类型）

- 1B serializer（序列化类型）

- 4B body length（body 长度）：为了避免 Socket 的粘包问题，在 header 中给出 body 的长度。

- 4B extension bytes（扩展字节）：待后续扩展，例如可以在 header 中存入 requestId 等字段

- body：常来说是请求的参数、响应的结果，再经过序列化、压缩后的字节数组（RpcRequest 或者 RpcResponse 实例对象）

为该自定义编写编码器和解码器，编码器主要是将协议定义的各个字段写出，而解码器则是按顺序读入各个字段。

此外，还会根据字段的信息进行序列化和压缩等操作。

## 语义

### 1. Client: At-Least-Once

Client 实现 `At-Least-Once` 语义，将在响应超时时，重传请求，默认设置为 3 次，可以在 `/ya-rpc-common/config/CommonConfig` 中对参数 `retry` 进行修改。

为了使 `At-Least-Once` 能够正常使用，需要保证下述二者之一即可：

- 仅有读操作，不进行写操作
- Server 采用一定的方式应对重复和重排序（实现 `At-Most-Once` 语义）

### 2. Server At-Most-Once

Server 实现 `At-Most-Once` 语义，当收到 Client 的重复请求时，仅执行一次请求。

这是十分必要的，除了节省服务器资源外，还可以避免多次执行 `写请求操作` 而导致的错误问题。

`ya-rpc` 实现了简单的重复请求判断，其伪代码如下：

```
Map<requestId, RpcResponse> cache;
if cache.containsKey(rId):
    res = cache.get(rId);
else:
    res = handler()  // 处理程
    cache.put(rId, res);
return res
```

上述代码存在三个细节问题：

- cache 无限增长：Client 在 RPC 请求时包含语义："seen all replies ≤ X"，Server 收到时删除此前所有请求的缓存。
- 当请求 A 正在执行时，如何处理重复请求 A2：A 处理时尚未写入 cacheMap 中，此时应该为正在执行的 RPC 请求新增标记符 `pending`。A2 发现是 `pending` 状态时，等待其处理结束。
- Server 崩溃或重启，导致内存中维护的 cacheMap 丢失：将 cacheMap 写入磁盘持久化处理。

`ya-rpc` 框架并未着力于完美的实现上述三个细节问题，仅考虑了最简单的情况。

对于 cache 无限增长问题，通过一个钩子函数在 20 秒后删除写入的缓存。20 秒的选择是因为 Client 只会重传 3 次，且超时时间为 5 秒。

对于另外两个问题，并未进行处理，因此 Client **只能发送读请求**，否则当遇到第二个细节问题时，将会重复执行。

## 功能列表

- [x] SPI 增强扩展
- [x] 注册中心 zookeeper
- [x] 序列化 protostuff、hessian
- [x] 压缩 gzip
- [x] 自定义协议
- [x] 网络传输
  - [x] Socket
  - [ ] Netty(Channel、心跳机制避免 Client/Server 重连)
- [ ] Spring 继承
  - [ ] 通过注解注册服务
  - [ ] 通过注解消费服务
- [ ] 服务监控中心
