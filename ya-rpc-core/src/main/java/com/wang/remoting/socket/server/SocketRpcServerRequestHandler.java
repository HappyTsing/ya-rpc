package com.wang.remoting.socket.server;

import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.dto.ServiceSignature;
import com.wang.exception.RpcClientException;
import com.wang.exception.RpcServerException;
import com.wang.remoting.RequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * @author happytsing
 */
@Slf4j
public class SocketRpcServerRequestHandler implements RequestHandler {

    private final Socket socket;

    public SocketRpcServerRequestHandler(Socket socket) {
        this.socket= socket;
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
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object result = handle(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (RuntimeException | ClassNotFoundException | IOException e) {
            throw new RpcServerException(e);
        }
    }
}
