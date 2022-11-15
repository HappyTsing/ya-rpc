package com.wang.remoting.socket.server;

import com.wang.config.CommonConfig;
import com.wang.extension.ExtensionLoader;
import com.wang.registry.Registry;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务器关机时，清除其在注册中心注册的所有服务
 * @author happytsing
 */
@Slf4j
public class ShutdownHook {
    public static void addShutdownHook() {
        log.info("addShutdownHook for clear all services registered with this machine.");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Registry registry = ExtensionLoader.getExtensionLoader(Registry.class).getExtension(CommonConfig.registry);
            registry.unregisterAllMyService();
//            registry.unregisterAllService();
            log.info("Clear all services registered with this machine success.");
        }));
    }
}

