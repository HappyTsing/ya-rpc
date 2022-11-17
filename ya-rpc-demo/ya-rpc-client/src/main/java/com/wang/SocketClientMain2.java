package com.wang;

import com.wang.pojo.User;
import com.wang.remoting.socket.client.SocketRpcClient;
import com.wang.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketClientMain2 {
    public static void main(String[] args) {
        SocketRpcClient client = new SocketRpcClient();
        UserService userService = client.getStub(UserService.class,"groupName1","version1");
        User userGetById = userService.getUserById(22080626);
        User userGetByName = userService.getUserByName("toucher le port");
        log.info("User1: {} User2: {}",userGetById,userGetByName);
    }
}
