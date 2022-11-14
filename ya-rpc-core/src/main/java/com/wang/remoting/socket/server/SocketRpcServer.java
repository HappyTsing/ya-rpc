package com.wang.remoting.socket.server;

import com.wang.dto.ServiceSignature;
import com.wang.exception.RpcClientException;
import com.wang.exception.RpcServerException;
import com.wang.extension.ExtensionLoader;
import com.wang.registry.Registry;
import com.wang.remoting.RpcServer;
import com.wang.utils.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;

/**
 * @author happytsing
 */
@Slf4j
public class SocketRpcServer implements RpcServer {
    private final Registry registry;
    private final ExecutorService threadPool;

    // todo 从配置引入
    public static final int PORT = 8585;

    public SocketRpcServer() {
        this.threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        this.registry = ExtensionLoader.getExtensionLoader(Registry.class).getExtension("zookeeper");
    }

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

    @Override
    public void start() {
        try (ServerSocket server = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, PORT));
            Socket socket;
            while ((socket = server.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRpcServerRequestHandler(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            throw new RpcServerException("Start server fail.", e);
        }
    }

}
