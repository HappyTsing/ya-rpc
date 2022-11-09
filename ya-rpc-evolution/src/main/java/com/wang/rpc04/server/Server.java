package com.wang.rpc04.server;


import com.wang.api.pojo.User;
import com.wang.api.service.UserService;
import com.wang.rpc03.server.service.impl.UserServiceImpl;
import com.wang.rpc04.server.service.impl.UtilServiceImpl;
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
        // update-rpc04: 读入接口类名
        ObjectInputStream oin = new ObjectInputStream(client.getInputStream());
        String className = oin.readUTF();
        String methodName = oin.readUTF();
        Class[] parameterTypes = (Class[])oin.readObject();
        Object[] args = (Object[])oin.readObject();

        // 调用方法
        // update-rpc03: 通过反射调用传入的方法
        ObjectOutputStream oout = new ObjectOutputStream(client.getOutputStream());

        Class clazz = null;
        // update-rpc04: 从服务注册表找到具体的类，也可以通过依赖注入实现。
        // 此处直接写死，演示用
        logger.info("className = " + className);
        switch (className){
            case "com.wang.api.service.UserService":{
                clazz = UserServiceImpl.class;
                break;
            }
            case "com.wang.api.service.UtilService":{
                clazz = UtilServiceImpl.class;
                break;
            }
            default:{
                logger.error("该类在注册表中不存在");
                clazz = null;
            }
        }
        Method method = clazz.getMethod(methodName,parameterTypes);
        Object o = (Object)method.invoke(clazz.getDeclaredConstructor().newInstance(),args);

        // 回传结果
        // update-rpc03: 序列化对象
        oout.writeObject(o);
        oout.flush();
    }
}
