package com.wang.remoting.socket.server;

import com.wang.config.CommonConfig;
import com.wang.extension.ExtensionLoader;
import com.wang.registry.Registry;
import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 钩子函数
 * @author happytsing
 */
@Slf4j
public class ServerHook {

    /**
     * 关机时取消注册本机的所有服务
     */
    public static void addShutdownHook() {
        log.info("addShutdownHook for clear all services registered with this machine.");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Registry registry = ExtensionLoader.getExtensionLoader(Registry.class).getExtension(CommonConfig.registry);
            registry.unregisterAllMyService();
//            registry.unregisterAllService();
            log.info("Clear all services registered with this machine success.");
        }));
    }

    /**
     * 为了实现服务端的 at most once，因此缓存请求的处理。
     * 为了避免缓存的无限增长，在 20 秒后删除缓存。
     * @param requestId 客户端请求 id
     */
    public static void addRemoveResponseCachHook(String requestId){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(SocketRpcServerRequestHandler.RESPONSE_CACHE);
                SocketRpcServerRequestHandler.RESPONSE_CACHE.remove(requestId);
                System.out.println(SocketRpcServerRequestHandler.RESPONSE_CACHE);
            }
        },20000);
    }
}

