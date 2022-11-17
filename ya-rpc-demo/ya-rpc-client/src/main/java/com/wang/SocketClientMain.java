package com.wang;

import com.wang.remoting.socket.client.SocketRpcClient;
import com.wang.service.UtilService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketClientMain {
    public static void main(String[] args) {
        SocketRpcClient client = new SocketRpcClient();
        UtilService utilService = client.getStub(UtilService.class,"groupName1","version1");
        float sum = utilService.sum((float) 20.08, (float) 06.26);
        String uppercase = utilService.uppercase("happytsing");
        log.info("sum: {} uppercase: {}",sum,uppercase);
    }
}
