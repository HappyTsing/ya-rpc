package com.wang.remoting;

import com.wang.dto.RpcMessage;

import java.net.SocketTimeoutException;

/**
 * 用于发起请求，动态代理的类的方法执行时会调用
 * @author happytsing
 */
public interface RpcClient {
    /**
     *
     * @param rpcMessage 发起 RPC 请求
     * @return 响应结果
     */
    Object send(RpcMessage rpcMessage);

//    <T> RpcResponse<T> handleRpcResponse(RpcResponse<T> rpcResponse);

    /**
     * 获取动态代理对象
     * @param clazz 代理的接口
     * @param group 组
     * @param version 版本
     * @return 代理对象
     */
    <T> T getStub(Class<T> clazz,String group,String version);



}
