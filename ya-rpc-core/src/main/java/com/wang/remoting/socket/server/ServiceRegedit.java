package com.wang.remoting.socket.server;

import com.wang.dto.ServiceSignature;
import com.wang.exception.RpcClientException;
import com.wang.exception.RpcServerException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * key 注册的服务签名
 * value 服务实现的class
 */
@Slf4j
public class ServiceRegedit {
    private static final Map<ServiceSignature, Class> SERVICE_REGEDIT = new ConcurrentHashMap<>();
    public static  <T> void put(ServiceSignature serviceSignature, Class<T> clazz){
        SERVICE_REGEDIT.put(serviceSignature, clazz);
        log.info("Update ServiceRegedit success. The record is [ServiceSignature={}, clazz={}]",serviceSignature,clazz);
    }
    public static <T> Class<T> get(ServiceSignature serviceSignature) {
        Object service = SERVICE_REGEDIT.get(serviceSignature);
        if (service == null) {
            throw new RpcServerException("rpcService not found." + serviceSignature);
        }
        return (Class<T>) service;
    }
}
