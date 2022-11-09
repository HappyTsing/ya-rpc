package com.wang.rpc02.server;


import com.wang.api.pojo.User;
import com.wang.api.service.UserService;
import com.wang.rpc03.server.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author happytsing
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static boolean running = true;
    public static void main(String[] args) throws IOException {
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
    private static void process(Socket client) throws IOException{

        // 获取 Client 传来的参数
        DataInputStream dio = new DataInputStream(client.getInputStream());
        int userId = dio.readInt();

        // 调用方法
        UserService service = new UserServiceImpl();
        User user = service.getUserById(userId);

        // 回传结果
        DataOutputStream dout = new DataOutputStream(client.getOutputStream());
        dout.writeInt(user.getUserId());
        dout.writeUTF(user.getUserName());
        dout.flush();
    }
}
