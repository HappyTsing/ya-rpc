package com.wang;

import com.wang.dto.ServiceSignature;
import com.wang.remoting.socket.server.SocketRpcServer;
import com.wang.serviceImpl.groupName1.version1.UserServiceImpl;
import com.wang.serviceImpl.groupName1.version1.UtilServiceImpl;

public class SocketServerMain {
    public static void main(String[] args) {
        SocketRpcServer server = new SocketRpcServer();
        server.registerService(new ServiceSignature(UserServiceImpl.class,"groupName1","version1"), UserServiceImpl.class);
        server.registerService(new ServiceSignature(new UtilServiceImpl(),"groupName1","version1"), UtilServiceImpl.class);
        server.start();
    }
}
