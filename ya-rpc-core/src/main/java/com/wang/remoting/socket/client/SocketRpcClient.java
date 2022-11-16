package com.wang.remoting.socket.client;

import com.wang.config.CommonConfig;
import com.wang.dto.RpcMessage;
import com.wang.enums.CompressorTypeEnum;
import com.wang.enums.MessageTypeEnum;
import com.wang.enums.SerializerTypeEnum;
import com.wang.exception.RpcClientException;
import com.wang.extension.ExtensionLoader;
import com.wang.registry.Registry;
import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.remoting.RpcClient;
import com.wang.remoting.socket.codec.SocketRpcMessageCodec;
import io.protostuff.Rpc;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

/**
 * 客户端
 * @author happytsing
 */
@Slf4j
public class SocketRpcClient implements RpcClient {
    private final Registry registry;
    private final SocketRpcMessageCodec rpcMessageCodec;

    public SocketRpcClient() {
        this.registry = ExtensionLoader.getExtensionLoader(Registry.class).getExtension(CommonConfig.registry);
        this.rpcMessageCodec = new SocketRpcMessageCodec();
    }

    @Override
    public Object send(RpcMessage rpcMessage) {
        RpcRequest rpcRequest = (RpcRequest) rpcMessage.getData();
        InetSocketAddress inetSocketAddress = registry.lookup(rpcRequest);
        try (Socket client = new Socket()) {
            // 套接字连接超时时间
            int connectTimeout = 5000;
            client.connect(inetSocketAddress,connectTimeout);

            // 套接字连接后，读请求的超时时间
            int readTimeout = 5000;
            client.setSoTimeout(readTimeout);

            // 编码协议，并通过 socket 输出流将 RpcRequest 传输给服务端
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
            rpcMessageCodec.encode(rpcMessage,dout);

            DataInputStream din = new DataInputStream(new BufferedInputStream(client.getInputStream()));
            // 从 socket 输入流中解码协议，获取 RpcResponse
            RpcResponse rpcResponse = (RpcResponse) rpcMessageCodec.decode(din);
            return rpcResponse;
        } catch (IOException e){
            throw new RpcClientException(e);
        }
    }

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
                    RpcResponse<Object> rpcResponse = null;
                    int retryTimes = 0;
                    boolean flag = false;
                    while(retryTimes < CommonConfig.retry){
                        try{
                            rpcResponse = (RpcResponse<Object>) send(rpcMessage);
                            flag = true;
                            break;
                        }catch (RpcClientException e){
                            if(e.getMessage().contains("SocketTimeoutException")){
                                retryTimes++;
                                log.info("Wait Response timeout, Retry times: {}", retryTimes);
                            }else {
                                throw new RpcClientException("Unknown fail.");
                            }
                        }
                    }
                    if(flag && rpcResponse.getCode() == 200){
                        return rpcResponse.getData();
                    }else{
                        throw new RpcClientException("Get response fail. Retry times: " + retryTimes);
                    }
                }
        );
    }

}
