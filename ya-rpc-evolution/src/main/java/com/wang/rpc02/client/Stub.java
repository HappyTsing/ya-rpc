package com.wang.rpc02.client;

import com.wang.api.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Stub {
    private static final Logger logger = LoggerFactory.getLogger(Stub.class);
    public User getUserById(int id) throws Exception {
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
            dout.writeInt(id);
            dout.flush();
            logger.info("send userId to rpc server");

            // 模拟获取 RPC 请求结果
            DataInputStream din = new DataInputStream(client.getInputStream());
            int userId = din.readInt();
            String userName = din.readUTF();
            User user = new User(userId,userName);
            logger.info("response from rpc server: " + user);
            return user;
        }
    }
}
