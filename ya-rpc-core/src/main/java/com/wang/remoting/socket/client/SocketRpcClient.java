package com.wang.remoting.socket.client;

import com.wang.exception.RpcClientException;
import com.wang.extension.ExtensionLoader;
import com.wang.registry.Registry;
import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.remoting.AbstractRpcClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author happytsing
 */
public class SocketRpcClient extends AbstractRpcClient {
    private final Registry registry;

    public SocketRpcClient() {
        this.registry = ExtensionLoader.getExtensionLoader(Registry.class).getExtension("zookeeper");
    }

    @Override
    public <T> RpcResponse<T> sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = registry.lookup(rpcRequest);
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // Send data to the server through the output stream
            objectOutputStream.writeObject(rpcRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // Read RpcResponse from the input stream
            return (RpcResponse<T>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcClientException(e);
        }
    }

}
