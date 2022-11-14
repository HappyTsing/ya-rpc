package com.wang.remoting;

import com.wang.dto.ServiceSignature;

/**
 * @author happytsing
 */
public interface RpcServer {

    /**
     * 注册一个服务到注册中心
     * @param serviceSignature 服务签名
     * @param serviceClass 服务类，在 RPC 中，服务就是接口的实现。
     */
    <T> void registerService(ServiceSignature serviceSignature, Class<T> serviceClass);

    /**
     * 启动服务端
     */
    void start();


}
