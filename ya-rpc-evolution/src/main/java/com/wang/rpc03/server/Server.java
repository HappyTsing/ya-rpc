package com.wang.rpc03.server;


import com.wang.api.pojo.User;
import com.wang.api.service.UserService;
import com.wang.rpc03.server.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author happytsing
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static boolean running = true;
    public static void main(String[] args) throws Exception {
        try(ServerSocket server = new ServerSocket(8189)){
            while (running){
                logger.info("waiting for client connect...");
                try(Socket incoming = server.accept()){
                    logger.info("accept rpc request");
                    process(incoming);
                    logger.info("handle rpc request succeed");
                }
            }
        }
    }
    private static void process(Socket client) throws Exception {

        // 获取 Client 传来的参数
        // update-rpc03: 读入方法名、参数类型、参数值
        ObjectInputStream oin = new ObjectInputStream(client.getInputStream());
        String methodName = oin.readUTF();
        Class[] parameterTypes = (Class[])oin.readObject();
        Object[] args = (Object[])oin.readObject();

        // 调用方法
        // update-rpc03: 通过反射调用传入的方法
        ObjectOutputStream oout = new ObjectOutputStream(client.getOutputStream());
        UserService service = new UserServiceImpl();
        Method method = service.getClass().getMethod(methodName,parameterTypes);
        User user = (User)method.invoke(service,args);

        // 回传结果
        // update-rpc03: 序列化对象
        oout.writeObject(user);
        oout.flush();
    }
}
