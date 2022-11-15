package com.wang.remoting.socket.client;

import com.wang.config.CommonConfig;
import com.wang.dto.RpcMessage;
import com.wang.exception.RpcClientException;
import com.wang.extension.ExtensionLoader;
import com.wang.registry.Registry;
import com.wang.dto.RpcRequest;
import com.wang.dto.RpcResponse;
import com.wang.remoting.AbstractRpcClient;
import com.wang.remoting.socket.codec.SocketRpcMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 客户端
 * @author happytsing
 */
@Slf4j
public class SocketRpcClient extends AbstractRpcClient {
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
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);

            // 编码协议，并通过 socket 输出流将 RpcRequest 传输给服务端
            DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            rpcMessageCodec.encode(rpcMessage,dout);
            DataInputStream din = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            // 从 socket 输入流中解码协议，获取 RpcResponse
            RpcResponse rpcResponse = (RpcResponse) rpcMessageCodec.decode(din);

            return rpcResponse;
        } catch (IOException e) {
            throw new RpcClientException(e);
        }
    }

}
