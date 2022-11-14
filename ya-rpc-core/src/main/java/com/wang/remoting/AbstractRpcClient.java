package com.wang.remoting;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;

import java.lang.reflect.Proxy;


/**
 * @author happytsing
 */
public abstract class AbstractRpcClient implements RpcClient {

    public abstract <T> RpcResponse<T> sendRpcRequest(RpcRequest rpcRequest);

    @Override
    public <T> T getStub(Class<T> clazz, String group, String version) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                (proxy, method, args) ->{
                    RpcRequest rpcRequest = RpcRequest.builder()
                            .interfaceName(method.getDeclaringClass().getCanonicalName())
                            .methodName(method.getName())
                            .group(group)
                            .version(version)
                            .params(args)
                            .paramTypes(method.getParameterTypes())
                            .build();
                    RpcResponse<Object> rpcResponse = sendRpcRequest(rpcRequest);
                    return rpcResponse.getData();
                }
        );
    }

}
