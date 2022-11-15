package com.wang.remoting.socket.server;

import com.wang.config.CommonConfig;
import com.wang.dto.RpcMessage;
import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.dto.ServiceSignature;
import com.wang.enums.CompressorTypeEnum;
import com.wang.enums.MessageTypeEnum;
import com.wang.enums.SerializerTypeEnum;
import com.wang.exception.RpcServerException;
import com.wang.remoting.RequestHandler;
import com.wang.remoting.socket.codec.SocketRpcMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * @author happytsing
 * 处理 RPC 请求，主要进行如下操作:
 * 1. 从 Socket 输入流中 反序列化、解压 RpcRequest
 * 2. 序列化、压缩  RpcResponse，写入 Socket 输出流
 */
@Slf4j
public class SocketRpcServerRequestHandler implements RequestHandler {

    private final Socket socket;
    private final SocketRpcMessageCodec rpcMessageCodec;


    public SocketRpcServerRequestHandler(Socket socket) {
        this.socket= socket;
        this.rpcMessageCodec = new SocketRpcMessageCodec();
    }

    @Override
    public Object handle(RpcRequest rpcRequest){
        try{
            ServiceSignature serviceSignature = rpcRequest.getServiceSignature();
            Class clazz = ServiceRegedit.get(serviceSignature);
            String methodName = rpcRequest.getMethodName();
            Class[] parameterTypes = rpcRequest.getParamTypes();
            Object[] params = rpcRequest.getParams();
            Method method = clazz.getMethod(methodName,parameterTypes);
            Object result = method.invoke(clazz.getDeclaredConstructor().newInstance(),params);
            return result;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | RuntimeException |
                 IllegalAccessException e) {
            throw new RpcServerException(e);
        }
    }

    @Override
    public void run() {
        log.info("server handle message from client by thread: [{}]", Thread.currentThread().getName());
        try (DataOutputStream dout =new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             DataInputStream din =new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {

            log.info("server start decode");
            RpcRequest rpcRequest = (RpcRequest) rpcMessageCodec.decode(din);


            Object result = handle(rpcRequest);
            RpcResponse rpcResponse = RpcResponse.success(result,rpcRequest.getRequestId());
            RpcMessage rpcMessage = RpcMessage.builder()
                            .messageType(MessageTypeEnum.RESPONSE.getCode())
                            .compressType(CompressorTypeEnum.valueOf(CommonConfig.serverCompressor.toUpperCase()).getCode())
                            .serializeType(SerializerTypeEnum.valueOf(CommonConfig.serverSerializer.toUpperCase()).getCode())
                            .data(rpcResponse)
                            .build();

            log.info("server start encode");
            rpcMessageCodec.encode(rpcMessage,dout);

        } catch (RuntimeException | IOException e) {
            throw new RpcServerException(e);
        }
    }
}
