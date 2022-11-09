package com.wang.rpc03.client;

import com.wang.api.pojo.User;
import com.wang.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author happytsing
 * RPC 调用：调用接口 UserService 的方法 getUserById()，通过 userId 获取 User 实例。
 *          Server 中提供了 UserService 接口的实现
 *
 * Stub 为了能调用 UserService 中的所有方法，Stub 从静态代理改为动态代理实现。
 *      在静态代理的实现中，想要新增一个代理方法十分麻烦，需要修改 Stub 源码，而动态代理可以便携的增强所有 UserService 的所有方法。
 *      此处的增强，其实就是 RPC 远程调用。
 *
 * Client 调用静态方法 Stub.getStub() 获取 UserService 实例，可以通过该实例 RPC 调用 UserService 接口中的任何方法。
 *
 * Server 通过序列化的方法直接返回 User 对象，而不是返回 User 对象的各个属性
 *        此时就算 User 对象发生改变（例如增加属性），Stub 也无需修改代码，反序列即可直接获取 User user = (User) oin.readObject();
 *
 * 优化 rpc01 内容：
 *      1. 抽象出 Stub 类，解耦业务代码和RPC调用代码
 *         此时，开发者无需关心 Client/Server 的交互细节，只需要调用 Stub 即可。（当然，Stub 的开发者还是需要了解的）
 *
 * 优化 rpc02 内容
 *      1. Stub 采用动态代理实现，此时可以通过 Stub 远程调用 UserService 接口的所有方法
 *      2. Server 将对象序列化，将序列化二进制数据直接返回，Stub 反序列即可直接获取 User。
 *
 * 下版本待解决不足之处：
 *      1. Stub 只能处理 UserService 接口，而不能对第二个接口，例如 UtilService 中的方法进行处理
 *      2. Server 返回的数据是 User 类，而不能是其他格式的数据。
 */ 
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    public static void main(String[] args) throws Exception {
        UserService service = Stub.getStub();
        User user1 = service.getUserById(22080626);
        User user2 = service.getUserByName("toucherport");
        logger.info("getUserById + " + user1);
        logger.info("getUserByName + " + user2);
    }
}
