package com.wang.rpc02.client;

import com.wang.api.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author happytsing
 * RPC 调用：调用接口 UserService 的方法 getUserById()，通过 userId 获取 User 实例。
 *          Server 中提供了 UserService 接口的实现
 *
 * 抽象出 Stub，本质是静态代理。但是在RPC中由于习惯，Client端的代理称Stub，Server端的代理称Skeleton。
 *
 * Stub 将 rpc01 中网络的交互抽象出来，由于RPC调用的方法是getUserById(),于是在 Stub 中提供了一个同名的方法，它会去与 Server 交互，重组数据，构建实例。
 * Client 此时只需要构建 new Stub() 实例，任何没有 Socket 编码经验的人都可以进行 RPC 调用了。
 * Server 未作任何改变。
 *
 * 优化 rpc01 内容
 *      1. 抽象出 Stub 类，解耦业务代码和RPC调用代码
 *         此时，开发者无需关心 Client/Server 的交互细节，只需要调用 Stub 即可。（当然，Stub 的开发者还是需要了解的）
 *
 * 下版本待解决不足之处：
 *      1. 只代理了getUserById方法，当需要代理新的方法时，例如 UserService 的第二个方法 getUserByName()，需要对Stub进行大幅代码添加
 *      2. Server 返回给 Client 的数据是 User 对象的各个属性，此时一旦 User 发生变化（例如添加了新的属性）
 *  *      此时由于 Client 是通过获取所有的属性，然后 User user = new User(param1, param2...) 来构建的，因此也需要做相应的修改。
 *
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    public static void main(String[] args) throws Exception {
        Stub stub = new Stub();
        User user = stub.getUserById(22080626);
        logger.info("User " + user);
    }
}
