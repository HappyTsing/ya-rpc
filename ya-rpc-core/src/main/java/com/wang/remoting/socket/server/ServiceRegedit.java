package com.wang.remoting.socket.server;

import com.wang.dto.ServiceSignature;
import com.wang.exception.RpcServerException;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册表
 * Server 向注册中心注册服务时，会更新该表，添加一条记录。
 * Client 向 Server 请求服务时，Server 根据请求的数据，从注册表中拿出服务接口的实现类.class，并基于此构建实现类实例，执行对应的方法。
 * key 注册的服务签名            例如 com.wang.HelloService?group=1&version=1
 * value 服务接口的实现类.class  例如 HelloServiceImpl1.class
 * @author happytsing
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
            throw new RpcServerException("Get from ServiceRegedit fail. The serviceSignature is {}" + serviceSignature);
        }
        return (Class<T>) service;
    }
}
