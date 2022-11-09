package com.wang.rpc01.client;

import com.wang.api.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author happytsing
 * RPC 调用：调用接口 UserService 的方法 getUserById()，通过 userId 获取 User 实例。
 *          Server 中提供了 UserService 接口的实现
 *
 * Client 将参数 userId 转换为二进制数据，通过 Socket 发送给 Server。
 * Server 服务端接收到参数，调用方法 User user =  UserServiceImpl.getUserById(userId)
 *        并将处理的结果 user 的各个属性依次转换为二进制数据，通过 Socket 响应给 Client。
 * Client 将二进制数据转换为Java内部的UTF-16编码，通过对象属性，使用 new Person(...) 构建一个新的对象，完成RPC调用。
 *
 * 下版本待解决不足之处：
 *      1. 业务代码和RPC调用代码耦合在一起
 *      ...
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    public static void main(String[] args) throws IOException {
        // 为了避免 Socket(String host, String port) 建立连接的超时问题，先建立空Socket，再为 connect 方法传入 timeout 即可
        try(Socket client = new Socket()){

            // 5秒超时时间
            int connectTimeout = 5000;
            client.connect(new InetSocketAddress("127.0.0.1",8189),connectTimeout);
            logger.info("Connected to 127.0.0.1:8189 succeed");

            // 套接字连接后，读请求的阻塞时间
            int readTimeout = 5000;
            client.setSoTimeout(readTimeout);

            // 模拟发起 RPC 请求
            DataOutputStream dout = new DataOutputStream(client.getOutputStream());
            dout.writeInt(22080626);
            dout.flush();
            logger.info("send userId to rpc server");

            // 模拟获取 RPC 请求结果
            DataInputStream din = new DataInputStream(client.getInputStream());
            int userId = din.readInt();
            String userName = din.readUTF();
            User user = new User(userId,userName);
            logger.info("response from rpc server: " + user);
        }
    }
}
