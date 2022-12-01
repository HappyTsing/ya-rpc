# RPC 简介

## 概念

分布式计算中，远程过程调用（RPC, Remote Procedure Call）是一个计算机通信协议。

该协议允许运行于一台计算机的程序调用另一个地址空间（通常是开放网络的计算机）的子程序，而程序员就像调用本地程序一样，无需额外地为这个交互作用编程。

RPC 是一种 Client/Server 模式。

如果涉及的软件采用面向对象编程，那么远程过程调用亦可称作**远程调用**或**远程方法调用**，例：Java RMI

## issues

- Heterogeneity：Client 必须与 Server 会话，Server 必须调度所需的 Function，如果 Server 是不同类型的机器怎么办？
- Failure：如果信息被丢弃怎么办？如果 Client、Server 或者 Network 发生故障怎么办？
- Performance：如何保证性能问题？

### issue one: Heterogeneity

关于 Heterogeneity 的问题，也就是数据表述的差异，这对 local procedure call 来说不是什么问题，但 remote procedure call 时，由于：

- Run programs written in **different languages**
- Represent data types using **different sizes**
- Use **different byte ordering** (endianness)
- Represent floating point numbers **differently**
- Have **different data alignment** requirements

上述问题的解决办法是 IDL（Interface Description Language），即以独立于机器的方式传递程序参数和返回值。

程序员在 IDL 中编写一个 interface description，其中定义了过程调用的 API：names, parameter/return types

然后运行 **IDL compiler** 来**生成**:

- 将本机数据类型转换为独立于及其的字节流代码，理解为序列化。

- **Client stub**: 将本地过程调用作为请求转发到服务器

- **Server stub**
  - <u>Dispatcher</u>：接受客户的 RPC 请求，确定对应的服务端方法来调用
  - <u>Skeleton</u>：对字节流代码进行反序列化，转化为服务器数据类型，调用本地服务器应用，对响应进行打包并发送给 Dispatcher
  - 注意：上述行为对程序员隐藏，且 Dispatcher 和 Skeleton 可以被整合到一起，取决于具体的实现。

> 在 RPC 中，例如在[dubbo](https://dubbo.apache.org/zh/docs3-v2/java-sdk/concepts-and-architecture/code-architecture/)中，对于 Proxy 服务代理层，**<u>☆ 客户端的 Proxy 称为 Stub，服务端的 Proxy 称为 Skeleton</u>**。
>
> [wiki](<https://zh.wikipedia.org/zh-tw/%E6%A1%A9_(%E8%AE%A1%E7%AE%97%E6%9C%BA)>)对于 Stub 的介绍如下：
>
> - 在远程方法调用（RMI）中將客户辅助对象称为 Stub；将服务辅助对象称为 skeleton（骨架）。
>
> Dispatcher 是否更相似于实际实现中的注册中心 Registry？

![rpc-example](https://happytsing-figure-bed.oss-cn-hangzhou.aliyuncs.com/rpc/rpc-example.png)

### issue two: Failure

RPC 过程中可能存在如下问题：

- Client 和 Server 可能故障或重启
- Packets 可能被丢弃，网络中的单独包丢失，路由问题导致许多包丢失等
- Network 和 Server 可能变得很慢，可能由于网络拥塞等原因

为了处理上述问题，一般来说有三种语义：

- At-Least-Once：针对 Client 而言，超时重传机制，即当 ack timeout 时，重新向 Server 发送请求
- At-Most-Once：针对 Server 而言，去重机制，保证 Sever 最多执行一次重复请求。
- Exactly-Once：针对整个 RPC 框架而言，结合超时重传和去重机制，在屏蔽硬件故障的情况下即可实现精确一次。

#### At-Least-Once：至少一次

针对 Client，最简单的错误处理方式，也就是超时重传：

- Client stub 等待响应，响应是 Server stub 发送的 **acknowledgement**
- 如果在一个固定的**timeout**都没有收到响应，那么 Client stub 重新发送一次请求，Sever stub 接受请求，并**重复执行**
- 重复数次，如果还是没有响应，返回 error 给应用

注意，并不会约束 Server 的行为，因此如果 Server 没有去重机制，那么就会重复执行，如果 Server 有去重机制，那么 Server 端也不会执行多次，因此称为至少一次！

但这种方式在某些情况下会出现问题：

![at-least-once-problem](https://happytsing-figure-bed.oss-cn-hangzhou.aliyuncs.com/rpc/at-least-once-problem.png)

由于超时，Client stub 发送了两次 put(x,10)，随后又发送了 put(x,20)，但由于 Server 接收信息的顺序不同，最后 Client 想要将 x 改成 20，但实际上改成了 10！

为了使 At-Least-Once 能够正常使用，需要保证下述二者之一即可：

- 仅有读操作，不进行写操作
- Server 采用一定的方式应对重复和重排序，**本次课题需要实现**！

#### At-Most-Once：最多一次

针对 Server 而言，去重机制，保证 Sever 最多执行一次重复请求。但是 Client 如果没有实现超时重传机制，那么如果请求在网络中丢失就会导致 Server 一次都不执行。

**Idea：**Server RPC 检测重复请求，返回上一次的执行结果，而不是重新执行请求。

**Question：**如何检测重复请求？

- Client 发起 RPC request 时，在请求中包含唯一的**transaction ID（XID）**

- Client 发起重复的 RPC request 时，使用与上一次请求相同的 XID

- Server 使用下述代码判断：

  ```c
  At-Most-Once Server
  if seen[xid]:      // seen 数组中存放了xid:true，即所有请求过的xid。用于判断一个请求是否是重复请求
  	res = old[xid]   // old 数组中存放xid:res，即所有的处理结果。重复请求时可以直接返回上次的执行结果
  else:
  	res = handler()  // 处理程序
  	old[xid] = res
  	seen[xid] = true
  return res
  ```

XID 的生成方式有：

- current time
- random number
- sequence number（需要考虑 Client 重启时能否使用相同的序列号）

**Problems**

上述的方式仍旧存在三个细节问题：

- seen[]和 old[]数组将会无限扩张。
  - 观察到当 Client 得到特定请求的响应后，不会在重复发送请求，因此：
  - 解决办法：Client 可以告诉 Server 已经获取到响应，并让 Server 删除该 XID 对应的数组信息
  - 例如：xid = <unique client id, sequence number>
    - <42,1000>, <42, 1001>, <42, 1002>
    - client 在 RPC 请求时包含语义："seen all replies ≤ X"
    - 此时 Server 在收到该 RPC 请求时，将 xid ≤ X 的数组信息全部删除
- 当请求 A 正在执行时，如何处理重复请求 A_2。
  - 此时，Server 不知道已经处理过（因为正在处理），当然我们也不想处理两次！
  - 解决办法：为正在执行的 RPC 请求，新增标记符 pending。Server 等待结束。
- Server 崩溃或重启将导致内存维护中的 seen[] 和 old[] 数组丢失。
  - 当上述两个数组丢失时，Server 将无法判断请求是否是重复请求
  - 解决办法：写入磁盘

#### Exactly-Once：精准一次

针对整个 RPC 框架的概念，当 Client 实现 at-least-once 的重传机制，Server 端实现 at-most-once 的去重机制，此时即为 Exactly-Once，更精确的来说，需要满足下述条件：

- at-least-once 的重传机制
- at-most-once 的去重机制
- fault tolerance
- no external actions：外部硬件行为很难保证是否成功

此外还有相似的概念：

- 幂等性：任意多次执行所产生的影响均与一次执行的影响相同
- 事务：事务范围内的所有操作都可以全部成功或者全部失败

通过幂等性和事务可以保证 Exactly-Once 语义。

# RPC 框架

一个最简单的 RPC 框架分成三个部分：注册中心、服务端、客户端。以下是一个最简单的结构流程图。

![rpc-structure](https://happytsing-figure-bed.oss-cn-hangzhou.aliyuncs.com/rpc/rpc-structure.png)

- Server/Provider：服务提供方
- Client/Consumer：调用远程服务的服务消费方
- Registry：服务注册与发现的注册中心

> Dubbo 中还有：
>
> - Monitor：统计服务的调用次数和调用时间的监控中心
> - Container：服务运行容器
>
> <img src="https://happytsing-figure-bed.oss-cn-hangzhou.aliyuncs.com/rpc/dubbo-rpc-structure.png" alt="dubbo框架" style="zoom:50%;" />
>
> 其调用关系为：
>
> 1. 服务容器负责启动，加载，运行服务提供者。
> 2. 服务提供者在启动时，向注册中心注册自己提供的服务。
> 3. 服务消费者在启动时，向注册中心订阅自己所需的服务。
> 4. 注册中心返回服务提供者地址列表给消费者，如果有变更，注册中心将基于长连接推送变更数据给消费者。
> 5. 服务消费者，从提供者地址列表中，基于软负载均衡算法，选一台提供者进行调用，如果调用失败，再选另一台调用。
> 6. 服务消费者和提供者，在内存中累计调用次数和调用时间，定时每分钟发送一次统计数据到监控中心。

---

在具体的实现过程中，还有些许重要的模块或概念：

- **序列化与反序列化**：序列化用于将数据结构或对象转换为二进制串，亦称为编码。反序列化则是将二进制串转换为数据结构或对象的过程，亦称为解码。RPC 涉及网络传输，因此需要对数据进行序列化。

  本框架提供了 `protostuff` 和 `hessian` 两种序列化方式。

- **压缩与解压**：将序列化后的字节码再次压缩，减少网络传输数据包的体积，降低通信成本。

  本框架采用 `gzip` 进行压缩。

- **传输协议**：RPC 远程调用实际上就是网络传输，常用协议有：

  - 应用层协议：HTTP1.1、HTTP2.0、WebSocket

    > WebSocket protocol 是 HTML5 一种新的协议。它实现了浏览器与服务器全双工通信(full-duplex)。一开始的握手需要借助 HTTP 请求完成。

  - 传输层协议：TCP

    > Socket 是对 TCP/IP 协议的封装，Socket 本身并不是协议，而是一个调用接口（API），通过 Socket，可以更方便地使用 TCP/IP 协议。
    >
    > 传统的 Socket 是基于 BIO 实现的，因此需要为每一次 Client 请求分配一个线程，否则就会阻塞。
    >
    > Netty 是基于 NIO 实现的网络编程框架，其底层也会使用 Socket。

  本框架提供 Socket 实现。

- **自定义协议**：就是客户端和服务端的约定的通信方式。

- **动态代理**：RPC 的核心

- **负载均衡**：将客户端的请求负载均衡到多个服务器上，避免单个服务器响应同一个请求而导致宕机、崩溃。

  本框架提供 `随机` 和 `轮询` 两种负载均衡策略。

## 序列化

**为什么需要序列化和反序列化？**

互联网的产生带来了机器间通讯的需求，而通讯的双方需要约定特定的协议，序列化和反序列化属于**通讯协议**的一部分。

通讯协议往往采用分层模型，且不同的模型分层粒度不同，例如：TCP/IP 网络通讯模型是一个四层协议，而 OSI 模型是七层协议模型。

OSI 模型中的**展示层的主要功能是把应用层的对象转换为一段连续的二进制串，或者反过来将二进制串转换为对象**。

TCP/IP 模型的应用层对应于 OSI 模型的应用层、展示层和会话层，所以序列化协议属于 TCP/IP 模型的应用层部分。

![osi](https://happytsing-figure-bed.oss-cn-hangzhou.aliyuncs.com/rpc/OSImodel.png)

---

**数据结构、对象与二进制串**

不同的计算机语言中，数据结构，对象以及二进制串的表示方式并不相同。

- 数据结构和对象：对于类似 Java 这种完全面向对象的语言，工程师操作的一切都是对象，来自于 class 的实例化。在 Java 中最接近数据结构的概念就是 POJO 或者 Javabean，即只有 setter/getter 方法的类。而在 C++ 这种半面向对象的语言中，数据结构和 struct 对应，对象和 class 对应。

- 二进制串：序列化所生成的二进制串指的是存储在内存中的一块数据。

  - C++ 语言具有内存操作符，所以二进制串的概念容易理解，例如，C++ 语言的字符串可以直接被传输层使用，因为其本质上就是以`\0` 结尾的存储在内存中的二进制串。

  - 在 Java 语言里面，二进制串的概念容易和 String 混淆。实际上 String 是 Java 的一等公民，是一种特殊对象（Object）。

    对于跨语言间的通讯，序列化后的数据当然不能是某种语言的特殊数据类型。

    **二进制串在 Java 里面所指的是 byte[]**，byte 是 Java 8 中的原生数据类型之一（Primitive data types）。

---

**序列化和反序列化的组件**

典型的序列化和反序列化过程往往需要如下组件：

- IDL（Interface description language）文件：参与通讯的各方需要对通讯的内容需要做相关的约定（Specifications）。为了建立一个与语言和平台无关的约定，这个约定需要采用与具体开发语言、平台无关的语言来进行描述。这种语言被称为接口描述语言（IDL），采用 IDL 撰写的协议约定称之为 IDL 文件。
- IDL Compiler：IDL 文件中约定的内容为了在各语言和平台可见，需要有一个编译器，将 IDL 文件转换成各语言对应的动态库。
- Stub/Skeleton Lib：负责序列化和反序列化的工作代码。
  - Stub 是一段部署在分布式系统客户端的代码，一方面接收应用层的参数，并对其序列化后通过底层协议栈发送到服务端，另一方面接收服务端序列化后的结果数据，反序列化后交给客户端应用层；
  - Skeleton 部署在服务端，其功能与 Stub 相反，从传输层接收序列化参数，反序列化后交给服务端应用层，并将应用层的执行结果序列化后最终传送给客户端 Stub。
- Client/Server：指的是应用层程序代码，他们面对的是 IDL 所生成的特定语言的 class 或 struct。
- 底层协议栈和互联网：序列化之后的数据通过底层的传输层、网络层、链路层以及物理层协议转换成数字信号在互联网中传递。

![idl](https://happytsing-figure-bed.oss-cn-hangzhou.aliyuncs.com/rpc/idlModel.jpg)

---

**序列化组件与数据库访问组件的对比**

数据库访问对于很多工程师来说相对熟悉，所用到的组件也相对容易理解。下表类比了序列化过程中用到的部分组件和数据库访问组件的对应关系，以便于大家更好的把握序列化相关组件的概念。

| 序列化组件        | 数据库组件  | 说明                                   |
| :---------------- | :---------- | :------------------------------------- |
| IDL               | DDL         | 用于建表或者模型的语言                 |
| DL file           | DB Schema   | 表创建文件或模型文件                   |
| Stub/Skeleton lib | O/R mapping | 将 class 和 Table 或者数据模型进行映射 |

---

常用的序列化协议有：

- XML：冗余且解析慢
  - SOAP：基于 XML 为序列化和反序列化协议的结构化消息传递协议
  - Web service：基于 SOAP 的解决方案
  - WSDL：SOAP 协议的主要接口描述语言（IDL）
  - XSD：WSDL 的描述文件，由于 XSD 本身就是一种 XML 文件，称为自我描述或递归。
- JSON：简洁且解析速度较快
  - JSON 序列化不需要 IDL，因为 IDL 的目的是撰写 IDL 描述文件，而 IDL 描述文件被 IDL Comipler 编译后能生成 Stub/Skeleton 的代码，这些代码是负责响应的序列化和反序列化工作的组件。由于 json 和一般语言的 class 太像了，例如 json 在 js 中就是类的概念，自身具备对其序列化的能力，对于 Java 这种强类型的语言，可以通过反射统一解决。
  - 总的来说，采用 JSON 进行序列化的**额外空间开销比较大**，对于大数据量服务或持久化，这意味着巨大的内存和磁盘开销，这种场景不适合。**没有统一可用的 IDL 降低了对参与方的约束**，实际操作中往往只能**采用文档方式来进行约定**，这可能会给调试带来一些不便，延长开发周期。 由于 JSON 在一些语言中的序列化和反序列化需要采用**反射**机制，所以**在性能要求为 ms 级别**，不建议使用。
- 二进制序列化：序列化后是字节数组
  - Java 平台：JDK Serializable、FST、Kyro。
  - 跨平台，接口描述语言（IDL）：Protobuf（描述文件.proto）、Thrift（描述文件.thrift）、Avro
    - **protostuff**：基于 protobuf，但无需写配置文件，开箱即用！
  - 跨平台，使用自描述完成服务定义：Hessian2，Hessian 自行定义了一套自己的储存和还原数据的机制
  - 跨平台，[使用 JSON 完成模式定义](https://www.gingerdoc.com/avro/avro_quick_guide)：Avro

Example：

```sh
# JAVA对象Person，序列化为JSON
{ "age": 30, "name": "zhangsan", "height": 175.33, "weight": 140 }
```

json 格式可读性强，但是数据冗余大，如果使用 protobuf 进行序列化的话：

- 省去冗余的{}"":等，同时省略 null 值
- 采用 tag 技术替代 json 的 k/v 格式，使用 tag 仅占用 1 个字节
- 对整数进行压缩
- ...

值得注意的是，使用 protobuf 需要实现 IDL 的接口描述文件.proto，基于该描述文件才能完成序列化。

> 美团的[序列化与反序列化](https://tech.meituan.com/2015/02/26/serialization-vs-deserialization.html)中给出了更多的示例，强烈推荐查阅！
>
> 且给出了时空消耗：
>
> - XML 序列化（Xstream）无论在性能和简洁性上比较差。
> - Thrift 与 Protobuf 相比在时空开销方面都有一定的劣势。
> - Protobuf 和 Avro 在两方面表现都非常优越。
>
> 有关**选型建议**可自行查阅文章。

## 注册中心 zookeeper

### 安装与启动

```sh
wget https://dlcdn.apache.org/zookeeper/zookeeper-3.7.1/apache-zookeeper-3.7.1-bin.tar.gz
tar -zxvf apache-zookeeper-3.7.1-bin.tar.gz
cp zoo_sample.cfg zoo.cfg # 配置文件
vim zoo.cfg # 将数据和日志修改成 zookeeper 安装目录
dataDir=[pwd]/data
dataLogDir=[pwd]/log

sh bin/zKserver.sh start # 单机节点 服务端
sh bin/zkCli.sh # 启动客户端
$ ls [path]
$ create -e /newNode 1 # 创建一个新节点其值为 12
$ set /newNode 2
$ get /newNode
```

当然，往往需要一个集群，而不是单机节点。可以通过 docker compose 快速构建。

```yaml
version: "3.1"

services:
  zoo1:
    image: zookeeper:3.7.1
    restart: always
    hostname: zoo1
    ports:
      - 2181:2181
    environment:
      ZOO_MY_ID: 1
      ZOO_SERVERS: server.1=0.0.0.0:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=zoo3:2888:3888;2181

  zoo2:
    image: zookeeper:3.7.1
    restart: always
    hostname: zoo2
    ports:
      - 2182:2181
    environment:
      ZOO_MY_ID: 2
      ZOO_SERVERS: server.1=zoo1:2888:3888;2181 server.2=0.0.0.0:2888:3888;2181 server.3=zoo3:2888:3888;2181

  zoo3:
    image: zookeeper:3.7.1
    restart: always
    hostname: zoo3
    ports:
      - 2183:2181
    environment:
      ZOO_MY_ID: 3
      ZOO_SERVERS: server.1=zoo1:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=0.0.0.0:2888:3888;2181
```

- image:镜像版本

- restart: 由于 Zookeeper 是「快速失败」，因此最好通过 `--retart` 参数设置容器在意外停止时自动重启。默认是不重启。

- ports：zoo1、zoo2、zoo3，并分别将本地的 2181, 2182, 2183 端口绑定到对应的容器的 2181 端口上

- ZOO_MY_ID: 该 id 在集群中必须是唯一的，并且其值应介于 1 和 255 之间；

  请注意，如果使用已包含 `myid` 文件的 `/data` 目录启动容器，则此变量将不会产生任何影响。相当于你在 `zoo.cfg` 中指定的`dataDir` 目录中创建文件 `myid`，并且文件的内容就是范围为 1 到 255 的整数；

- ZOO_SERVERS:此变量允许您指定 Zookeeper 集群的计算机列表；

  每个条目都应该这样指定：`server.id=<address>:<port1>:<port2>[:role];[<client port address>:]<client port>`

  - id 是一个数字，表示集群中的服务器 ID；
  - address 表示这个服务器的 ip 地址；
  - 集群通信端⼝: port1 表示这个服务器与集群中的 Leader 服务器交换信息的端口；
  - 集群选举端⼝: port2 表示万一集群中的 Leader 服务器挂了，需要一个端口来重新进行选举，选出一个新的 Leader，而这个端口就是用来执行选举时服务器相互通信的端口；
  - role: 默认是 participant，即参与过半机制的⻆⾊，选举，事务请求过半提交；另一种是 observer，即观察者，不参与选举以及过半机制；
  - client port address 是可选的，如果未指定，则默认为 `0.0.0.0` ；
  - client port 位于分号的右侧。从 3.5.0 开始，`zoo.cfg` 中不再使用 `clientPort` 和 `clientPortAddress` 配置参数。作为替代，`client port` 用来表示客户端连接 Zookeeper 服务器的端口；

通过 `docker-compose` 启动服务端集群：

```shell
#  COMPOSE_PROJECT_NAME=zookeeper_cluster 环境变量用于为 compose 工程起一个名字
COMPOSE_PROJECT_NAME=zookeeper_cluster docker-compose -f zookeeper_cluster.yml up -d

docker ps -a
docker exec -it zookeeper_cluster-zoo1-1 /bin/bash
zkServer.sh status # 查看启动状态，依次查询 zool_1、2、3，可知创建了一个 Leader 节点，两个 Follower 节点的集群。
```

随后可以通过客户端连接集群：

1. 在容器内连接

```shell
docker exec -it zookeeper_cluster-zoo1-1 /bin/bash
zkCli.sh -server localhost:2181
```

2. 本地连接

由于通过 `ports` 将 zoo1, zoo2, zoo3 的 2181 端口分别映射到了本地主机的 2181, 2182, 2183 端口上, 因此我们使用如下命令连接 Zookeeper 集群：

```shell
cd zookeeper/bin # 进入本地的 zookeeper bin 目录
sh zkCli.sh -server localhost:2181,localhost:2182,localhost:2183
```

3. Java 客户端

可以通过官方 API ，也可以通过 Curator 框架，后者更好用，本次框架实现采用 `Curator 5.4.0`。

☆ 注意！！！一定要注意 Curator 版本和 zookeeper 版本相对于（通过 Maven Curator 查看其依赖的 zookeeper 版本），`Curator 5.4.0` 必须使用 `Zookeeper 3.7.1`。

### 快速入门

zookeeper 集群中超过一半的节点正常即可提供正常提供服务，因此在实际生产环境中，一般取奇数个节点，且至少有 3 个节点。

zookeeper 中的概念：

- Client

- Server

  - Leader：负责协调节点，且可以选择不接收客户端的连接。
  - follower：Leader 挂掉时参与选举。
  - Observer：提高读性能，不参与选举。数据仅加载到内存中。

- Ensemble：服务器组。形成 ensemble 所需的最小节点数为 3。

- Znode

  - znode 主要有以下 3 种：

  - 持久性 (PERSISTENT): ZooKeeper 宕机，或者 client 宕机，这个 znode 一旦创建就不会丢失。
  - 临时性 (EPHEMERAL): ZooKeeper 宕机了，或者 client 在指定的 timeout 时间内没有连接 server，都会被认为丢失。
  - 顺序节点。顺序节点可以是持久的或临时的。当一个新的 znode 被创建为一个顺序节点时，ZooKeeper 通过将 10 位的序列号附加到原始名称来设置 znode 的路径。例如，如果将具有路径 /myapp 的 znode 创建为顺序节点，则 ZooKeeper 会将路径更改为 /myapp0000000001 ，并将下一个序列号设置为 0000000002。如果两个顺序节点是同时创建的，那么 ZooKeeper 不会对每个 znode 使用相同的数字。顺序节点在锁定和同步中起重要作用。

  > 在 RPC 框架中，选择临时性节点即可。

  - 每个 znode 都维护着一个 stat 结构。一个 stat 仅提供一个 znode 的元数据。它由版本号，操作控制列表(ACL)，时间戳和数据长度组成。你最多可以存储 1MB 的数据。

- Sessions（会话）

  - 会话对于 ZooKeeper 的操作非常重要。会话中的请求按 FIFO 顺序执行。一旦客户端连接到服务器，将建立会话并向客户端分配会话 ID 。
  - 客户端以特定的时间间隔发送心跳以保持会话有效。如果 ZooKeeper 集合在超过服务器开启时指定的期间（会话超时）都没有从客户端接收到心跳，则它会判定客户端死机。
  - 会话超时通常以毫秒为单位。当会话由于任何原因结束时，在该会话期间创建的临时节点也会被删除。

- Watches（监视）

  - 监视是一种简单的机制，使客户端收到关于 ZooKeeper 集合中的更改的通知。客户端可以在读取特定 znode 时设置 Watches。Watches 会向注册的客户端发送任何 znode（客户端注册表）更改的通知。

  - Znode 更改是与 znode 相关的数据的修改或 znode 的子项中的更改。只触发一次 watches。如果客户端想要再次通知，则必须通过另一个读取操作来完成。当连接会话过期时，客户端将与服务器断开连接，相关的 watches 也将被删除。

**zookeeper 工作流**

一旦 ZooKeeper 集合启动，它将等待客户端连接。客户端将连接到 ZooKeeper 集合中的一个节点。它可以是 leader 或 follower 节点。一旦客户端被连接，节点将向特定客户端分配会话 ID 并向该客户端发送确认。如果客户端没有收到确认，它将尝试连接 ZooKeeper 集合中的另一个节点。 一旦连接到节点，客户端将以有规律的间隔向节点发送心跳，以确保连接不会丢失。

- 如果客户端想要读取特定的 znode，它将会向具有 znode 路径的节点发送读取请求，并且节点通过从其自己的数据库获取来返回所请求的 znode。为此，在 ZooKeeper 集合中读取速度很快。
- 如果客户端想要将数据存储在 ZooKeeper 集合中，则会将 znode 路径和数据发送到服务器。连接的服务器将该请求转发给 leader，然后 leader 将向所有的 follower 重新发出写入请求。如果只有大部分节点成功响应，而写入请求成功，则成功返回代码将被发送到客户

### RPC 中的应用

注册中心存在的目的是，为了让 Client 知道提供服务的 Server 的地址。

在 ya-rpc 框架的设计中，Server 注册后在 Zookeeper 中的存储形式如下：

```sh
/ya-rpc-provider
		/com.wang.service.UserService?group=g1&version=v1
				/[127.0.0.1:8713, 127.0.0.1:8712]
		/com.wang.service.UtilService?group=g1&version=v1
				/[127.0.0.1:8713]
```

当 Client 调用接口 UserService 的方法时，由于 Server 可能提供了该接口的多种实现，在 RPC 中 称为**服务**，因此需要指定 `group` 和 `version`。

- group：分组
- version：版本号

Client 会从 Zookeeper 中获取提供特定实现的服务的服务器的地址，当然，可能会存在多个服务器提供该服务，通过**负载均衡**从多个服务器中选择一个，然后 Client 将请求发送给 Server，Server 将响应返回给 Client。

因此，最终的通信仍旧是在 Client 和 Server 中进行。

## 粘包和半包问题

TCP 协议会导致粘包和半包问题，这是**因为 TCP 是面向连接的传输协议，TCP 传输的数据是以流的形式，而流数据是没有明确的开始结尾边界，所以 TCP 也没办法判断哪一段流属于一个消息**。

粘包的主要原因：

- 发送方每次写入数据 < 套接字（Socket）缓冲区大小；
- 接收方读取套接字（Socket）缓冲区数据不够及时。

半包的主要原因：

- 发送方每次写入数据 > 套接字（Socket）缓冲区大小；
- 发送的数据大于协议的 MTU (Maximum Transmission Unit，最大传输单元)，因此必须拆包。

由于底层的 TCP 无法理解上层的业务数据，所以在底层是无法保证数据包不被拆分和重组的，这个问题只能通过上层的应用协议栈设计来解决。目前业界主流协议的解决方案如下：

1. 发送方和接收方规定固定大小的缓冲区，也就是发送和接收都使用固定大小的 byte[] 数组长度，当字符长度不够时使用空字符弥补；
2. 在 TCP 协议的基础上封装一层数据请求协议，既将数据包封装成数据头（存储数据正文大小）+ 数据正文的形式，这样在服务端就可以知道每个数据包的具体长度了，知道了发送数据的具体边界之后，就可以解决半包和粘包的问题了；
3. 以特殊的字符结尾，比如以 `\n` 结尾，这样我们就知道结束字符，从而避免了半包和粘包问题。

Netty 提供了个常用的抽象编码器：`MessageToByteEncoder<I>`，编码器不像解码器需要考虑粘包拆包，只需要将数据转换成协议规定的二进制格式发送即可。

如果使用 Socket 的话，Client 和 Server 需要根据约定好的顺序写出和读入。

## SPI

RPC 框架有很多可扩展的地方，如：序列化类型、压缩类型、负载均衡类型、注册中心类型等等。

假设框架提供的注册中心只有 `zookeeper`，但是使用者想用 `Eureka`，修改框架以支持使用者的需求显然不是好的做法。

最好的做法就是留下扩展点，让使用者可以不需要修改框架，就能自己去实现扩展。

JDK 提供了 SPI 机制用于扩展，但仍存在资源浪费、使用麻烦等缺陷，因此参考 dubbo 的增强 SPI。

增强 SPI 的使用方式，以序列化来举例：

- 定义接口，接口加上 `@SPI` 注解

```java
@SPI
public interface Serializer {
    byte[] serialize(Object object);
}
```

- 该接口的实现类，例如 `ProtostuffSerializer` 和 `HessianSerializer`。
- 配置文件，约定放置于 `resources/META-INF/extensions`，配置文件的名字是接口的位置。例如：`com.wang.serialize.Serializer`。

```java
hessian=com.wang.serialize.hessian.HessianSerializer
protostuff=com.wang.serialize.protostuff.ProtostuffSerializer
```

- 获取扩展类，此时可以只实例化想要的实现类

```java
public static void main(String[] args) {
		ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
    Serializer serializer = extensionLoader.getExtension("protostuff");
}
```

## 动态代理

动态代理是实现 RPC 的关键，从本质上来说，RPC 的目的就是让用户像使用本地方法一样调用 RPC 方法。

以 `ya-rpc` 框架的使用为例：

```java
public class SocketClientMain {
    public static void main(String[] args) {
        SocketRpcClient client = new SocketRpcClient();
        UserService userService = client.getStub(UserService.class,"g1","v1");
        User user = userService.getUserById(1);
        System.out.println(user);
    }
}
```

此处引入一个概念，即`服务签名`，将 `interfaceName` 、`group`、`version` 三者合称为服务的签名，可以唯一的定位服务。

首先获取一个 RPC 客户端，随后调用 `getStub()` 方法，输入服务签名，此时就获取了接口 `UserService` 的特定实现。

随后，就可以像调用本地方法一样进行调用了。

> 新手可能存在问题，那就是，UserService 接口是从哪里导入的呢？更具体说，服务提供者和服务消费者如何约定接口？
>
> 通常来说，在一个单独的模块中定义接口，服务提供者和服务消费者都通过 Maven 依赖来引用此模块。
>
> 在 `ya-rpc` 框架提供的 demo 中，存在一个模块 `ya-rpc-api`，其中约定了接口，随后`ya-rpc-client` 和 `ya-rpc-server` 模块都需要导入该模块。

本质来说，`getStub()` 其实就是获取一个**动态代理类**，当用户调用任意的方法时，例如 `userService.getUserById(1)`，此时会通过反射机制，获取：

- InterfaceName
- methodName：方法名
- params：参数
- paramTypes：参数类型列表

再结合传入的：

- group
- version

上述六个参数可以组成两个签名：

- 方法签名：methodName + paramTypes
- 服务签名：InterfaceName + group + version

首先根据 `服务签名` 从注册中心获取提供该服务的 Server 的地址（host:port）

随机生成的 requestId，构造 `RpcRequest` 请求实例，再根据框架配置客户端的序列化和压缩方式，结合自定义的协议，将二进制数据传输给 Server，主要分为 header 和 body 两部分。

Server 根据协议，解析 header：

- 魔数、协议版本：魔数用于初步判断是否符合协议，过滤一些脏请求。
- 序列化方式和反序列化方式
- body 长度：为了防止粘包问题
- 消息类型：request/response

获取 body 的二级制数据，通过解压缩和反序列化得到对象。值得注意的是，反序列化时需要提供对象类，因此需要根据消息类型（request/response）反序列化对应的对象。

Server 通过服务签名从注册表中得到服务类.class，结合方法签名找到调用的方法，输入参数调用方法，基于调用结果构建 `RpcReponse` 响应实例，再根据框架配置服务端的序列化和压缩方式，结合自定义的协议，将二进制数据传输给 Client，主要分为 header 和 body 两部分。

Client 获取响应，返回结果，完成 RPC 调用。

## 自定义协议

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

# Reference

- [RPC - 小新](https://www.cnblogs.com/chenchuxin/category/2010813.html)
- [RPC - Guide](https://github.com/Snailclimb/guide-rpc-framework)
- dubbo
  - [框架设计](https://dubbo.apache.org/zh/docs/v2.7/dev/design/)
  - [RPC 协议](https://dubbo.apache.org/zh/docs3-v2/java-sdk/reference-manual/protocol/)
  - [序列化漫谈](https://dubbo.apache.org/zh/docsv2.7/user/serialization/)
- [RPC 演化 - 马士兵](https://www.bilibili.com/video/BV1qT4y1a7Es)
- [RPC.pptx - 分布式系统](https://happytsing-file-bed.oss-cn-hangzhou.aliyuncs.com/rpc/rpc.pptx)
- [序列化和反序列化 - 美团](https://tech.meituan.com/2015/02/26/serialization-vs-deserialization.html)
- [zookeeper - 菜鸟教程](https://www.runoob.com/w3cnote/zookeeper-tutorial.html)
- [docker 搭建 zookeeper 单机/集群](https://cloud.tencent.com/developer/article/1680299)
- [Socket 粘包的三种解决方案](https://www.cnblogs.com/vipstone/p/14239160.html)
- [RPC 自定义网络协议](https://blog.csdn.net/hxj413977035/article/details/121559224)
