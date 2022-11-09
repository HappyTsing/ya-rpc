package com.wang.rpc03.client;

import com.wang.api.pojo.User;
import com.wang.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Stub {
    private static final Logger logger = LoggerFactory.getLogger(Stub.class);
    public static UserService getStub(){
        // update-rpc03: 使用动态代理，使可以通过 RPC 调用 UserService 的任何方法
        Object o = Proxy.newProxyInstance(
                UserService.class.getClassLoader(),
                new Class[] {UserService.class},
                (proxy, method, args) -> {
                    try(Socket client = new Socket()){

                        // 5秒超时时间
                        int connectTimeout = 5000;
                        client.connect(new InetSocketAddress("127.0.0.1",8189),connectTimeout);
                        logger.info("Connected to 127.0.0.1:8189 succeed");

                        // 套接字连接后，读请求的阻塞时间
                        int readTimeout = 5000;
                        client.setSoTimeout(readTimeout);

                        // 模拟发起 RPC 请求
                        // update-rpc03: 将对象序列化为二进制数据，传输给 Server
                        ObjectOutputStream oout = new ObjectOutputStream(client.getOutputStream());

                        // update-rpc03: 将方法签名（方法名、方法参数）、参数值 传输给 Sever，使用方法签名可避免重载问题
                        String methodName = method.getName();
                        Class[] parametersType = method.getParameterTypes();
                        oout.writeUTF(methodName);
                        oout.writeObject(parametersType);
                        oout.writeObject(args);
                        oout.flush();
                        logger.info("send userId to rpc server");

                        ObjectInputStream oin = new ObjectInputStream(client.getInputStream());
                        // 模拟获取 RPC 请求结果
                        // update-rpc03: 读取 Server 传输过来的对象的序列化数据，将其反序列化为对象
                        User user = (User) oin.readObject();
                        logger.info("response from rpc server: " + user);
                        return user;
                    }
                });
        return (UserService)o;
    }
}
