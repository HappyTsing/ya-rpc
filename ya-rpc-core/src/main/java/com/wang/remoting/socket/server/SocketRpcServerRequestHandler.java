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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * 请求处理结果的缓存
     */
    public static final Map<String,RpcResponse> RESPONSE_CACHE = new ConcurrentHashMap<>();

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
            // 解码客户端传来的协议流，经解压缩、反序列化获取 RpcRequest
            RpcRequest rpcRequest = (RpcRequest) rpcMessageCodec.decode(din);

            // 服务端实现 at most once 语义
            // TODO: 为了避免缓存无限增长，此处简单的使用一个延迟任务在 20 秒后删除缓存。
            //       更好的办法是 Client 在 RPC 请求时包含语义："seen all replies ≤ X"，让Server删除该 requestId 对应的缓存。
            //       此外，为正在执行的RPC请求，新增标记符 pending。Server等待结束。而不是重复执行。
            String requestId = rpcRequest.getRequestId();
            RpcResponse rpcResponse;

            if(RESPONSE_CACHE.containsKey(requestId)){
                rpcResponse = RESPONSE_CACHE.get(requestId);
                log.info("hit cache for request " + requestId);
            }else{
                Object result = handle(rpcRequest);
                rpcResponse = RpcResponse.success(result,rpcRequest.getRequestId());
                RESPONSE_CACHE.put(requestId,rpcResponse);
                ServerHook.addRemoveResponseCachHook(requestId);
            }
            RpcMessage rpcMessage = RpcMessage.builder()
                            .messageType(MessageTypeEnum.RESPONSE.getCode())
                            .compressType(CompressorTypeEnum.valueOf(CommonConfig.serverCompressor.toUpperCase()).getCode())
                            .serializeType(SerializerTypeEnum.valueOf(CommonConfig.serverSerializer.toUpperCase()).getCode())
                            .data(rpcResponse)
                            .build();
            rpcMessageCodec.encode(rpcMessage,dout);
        } catch (RuntimeException | IOException e) {
            throw new RpcServerException(e);
        }
    }
}
