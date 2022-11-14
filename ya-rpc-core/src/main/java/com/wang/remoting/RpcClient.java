package com.wang.remoting;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;

/**
 * 用于发起请求，动态代理的类的方法执行时会调用
 * @author happytsing
 */
public interface RpcClient {
    /**
     *
     * @param rpcRequest 发起 RPC 请求
     * @return 响应结果
     */
    <T> RpcResponse<T> sendRpcRequest(RpcRequest rpcRequest);

    /**
     * 获取动态代理对象
     * @param clazz 代理的接口
     * @param group 组
     * @param version 版本
     * @return 代理对象
     */
    <T> T getStub(Class<T> clazz,String group,String version);



}
