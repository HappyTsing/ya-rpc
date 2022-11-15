package com.wang.remoting;

import com.wang.config.CommonConfig;
import com.wang.dto.RpcMessage;
import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.enums.CompressorTypeEnum;
import com.wang.enums.MessageTypeEnum;
import com.wang.enums.SerializerTypeEnum;
import com.wang.exception.RpcClientException;

import java.lang.reflect.Proxy;
import java.util.UUID;


/**
 * @author happytsing
 */
public abstract class AbstractRpcClient implements RpcClient {

    public abstract Object send(RpcMessage rpcMessage);

    /**
     * 本质是一个代理类，当用户执行方法时，向服务端发起 RPC 请求，并返回 RpcResponse 响应结果。
     * @param clazz 代理的接口
     * @param group 组
     * @param version 版本
     * @return 接口实现类的执行结果
     */
    @Override
    public <T> T getStub(Class<T> clazz, String group, String version) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                (proxy, method, args) ->{
                    RpcRequest rpcRequest = RpcRequest.builder()
                            .requestId(UUID.randomUUID().toString())
                            .interfaceName(method.getDeclaringClass().getCanonicalName())
                            .methodName(method.getName())
                            .group(group)
                            .version(version)
                            .params(args)
                            .paramTypes(method.getParameterTypes())
                            .build();
                    RpcMessage rpcMessage = RpcMessage.builder()
                            .messageType(MessageTypeEnum.REQUEST.getCode())
                            .compressType(CompressorTypeEnum.valueOf(CommonConfig.clientCompressor.toUpperCase()).getCode())
                            .serializeType(SerializerTypeEnum.valueOf(CommonConfig.clientSerializer.toUpperCase()).getCode())
                            .data(rpcRequest)
                            .build();
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) send(rpcMessage);
                    if(rpcResponse.getCode() == 200){
                        return rpcResponse.getData();
                    }else{
                        throw new RpcClientException("Get response fail: "+rpcResponse.getCode());
                    }
                }
        );
    }

}
