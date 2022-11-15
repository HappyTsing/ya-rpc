package com.wang;

import com.wang.dto.ServiceSignature;
import com.wang.remoting.socket.server.SocketRpcServer;
import com.wang.serviceImpl.UserServiceImpl;

public class SocketServerMain {
    public static void main(String[] args) {
        SocketRpcServer server = new SocketRpcServer();
        ServiceSignature serviceSignature = new ServiceSignature(UserServiceImpl.class.getInterfaces()[0].getCanonicalName(),"g1","v1");
        server.registerService(serviceSignature,UserServiceImpl.class);
        server.start();
    }

}
