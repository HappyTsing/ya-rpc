package com.wang.remoting;

import com.wang.dto.RpcRequest;

/**
 * @author happytsing
 */
public interface RequestHandler extends Runnable {

    /**
     * 处理 RPC 请求
     * @param rpcRequest RPC 请求
     * @return
     */
    Object handle(RpcRequest rpcRequest);

}
