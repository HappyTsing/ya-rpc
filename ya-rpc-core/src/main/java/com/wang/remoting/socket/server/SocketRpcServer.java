package com.wang.remoting.socket.server;

import com.wang.config.CommonConfig;
import com.wang.dto.ServiceSignature;
import com.wang.exception.RpcServerException;
import com.wang.extension.ExtensionLoader;
import com.wang.registry.Registry;
import com.wang.remoting.RpcServer;
import com.wang.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;

/**
 * 服务端
 * @author happytsing
 */
@Slf4j
public class SocketRpcServer implements RpcServer {
    private final Registry registry;
    private final ExecutorService threadPool;
    public static final int PORT = CommonConfig.rpcServerPort;

    public SocketRpcServer() {
        this.threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        this.registry = ExtensionLoader.getExtensionLoader(Registry.class).getExtension(CommonConfig.registry);
    }

    /**
     * 注册一个服务
     * @param serviceSignature 服务签名
     * @param serviceClass 服务类，在 RPC 中，服务就是接口的实现。
     */
    @Override
    public <T> void registerService(ServiceSignature serviceSignature, Class<T> serviceClass){
        try{
            String host = InetAddress.getLocalHost().getHostAddress();
            registry.register(serviceSignature,new InetSocketAddress(host, PORT));
            ServiceRegedit.put(serviceSignature,serviceClass);
        }catch (UnknownHostException e){
            throw new RpcServerException("RegisterService fail.", e);
        }
    }

    /**
     * 启动一个 Server，监听 Client 的请求，并分配线程处理对应的 RPC 请求
     */
    @Override
    public void start() {
        ShutdownHook.addShutdownHook();
        try (ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, PORT));
            Socket incoming;
            while ((incoming = server.accept()) != null) {
                log.info("client connected {}", incoming.getInetAddress());
                threadPool.execute(new SocketRpcServerRequestHandler(incoming));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            throw new RpcServerException("Start server fail.", e);
        }
    }

}
