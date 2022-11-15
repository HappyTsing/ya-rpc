package com.wang;

import com.wang.pojo.User;
import com.wang.remoting.socket.client.SocketRpcClient;
import com.wang.service.UserService;

public class SocketClientMain {
    public static void main(String[] args) {
        SocketRpcClient client = new SocketRpcClient();
        UserService userService = client.getStub(UserService.class,"g1","v1");
        User user = userService.getUserById(1);
//        User user2 = userService.getUserById(2);
        System.out.println(user);
    }
}
