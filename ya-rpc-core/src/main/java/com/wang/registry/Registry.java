package com.wang.registry;

import com.wang.dto.RpcRequest;
import com.wang.dto.ServiceSignature;
import com.wang.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 注册中心
 * @author happytsing
 */
@SPI
public interface Registry {

    /**
     * 向注册中心注册服务。注册的节点（在zookeeper表示为路径）为serviceSignature，该节点的值是提供该服务的服务端的 ip:port
     */
    void register(ServiceSignature serviceSignature, InetSocketAddress inetSocketAddress);

    /**
     * 向注册中心取消注册服务
     */
    void unregister(ServiceSignature serviceSignature,InetSocketAddress inetSocketAddress);

    /**
     * 查找注册的服务，返回的服务是通过负载均衡得到的结果
     *
     * @param rpcRequest 查询条件
     * @return 符合查询条件的所有注册者
     */
    InetSocketAddress lookup(RpcRequest rpcRequest);

    /**
     * 监听某服务
     */
    void watch(ServiceSignature  serviceSignature);
}