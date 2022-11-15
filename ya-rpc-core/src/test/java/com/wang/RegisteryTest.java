package com.wang;

import com.wang.dto.ServiceSignature;
import com.wang.extension.ExtensionLoader;
import com.wang.registry.Registry;
import com.wang.registry.zookeeper.CuratorUtils;
import com.wang.registry.zookeeper.ZookeeperRegistry;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.junit.Test;

import java.net.InetSocketAddress;

public class RegisteryTest {

    @Test
    public void register(){
        ZookeeperRegistry registry = (ZookeeperRegistry) ExtensionLoader.getExtensionLoader(Registry.class).getExtension("zookeeper");
        ServiceSignature serviceSignature = new ServiceSignature("com.wang.HelloService","1","1");
        InetSocketAddress inetSocketAddress = new InetSocketAddress("www.baidu.com",8080);
        System.out.println(inetSocketAddress.getAddress().getHostAddress());
        System.out.println(inetSocketAddress.toString());
        registry.register(serviceSignature,inetSocketAddress);
    }
    @Test
    public void unregister(){
        ZookeeperRegistry registry = (ZookeeperRegistry) ExtensionLoader.getExtensionLoader(Registry.class).getExtension("zookeeper");
        ServiceSignature serviceSignature = new ServiceSignature("com.wang.HelloService","1","1");
        InetSocketAddress inetSocketAddress = new InetSocketAddress("www.baidu.com",8080);
        System.out.println(inetSocketAddress.getAddress().getHostAddress());
        System.out.println(inetSocketAddress.toString());
        registry.unregister(serviceSignature,inetSocketAddress);
    }

    @Test
    public void watch(){
        ZookeeperRegistry registry = (ZookeeperRegistry) ExtensionLoader.getExtensionLoader(Registry.class).getExtension("zookeeper");
        ServiceSignature serviceSignature = new ServiceSignature("com.wang.HelloService","1","1");
        registry.watch(serviceSignature);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("1.2.3.4",8080);
        registry.register(serviceSignature,inetSocketAddress);
        registry.unregister(serviceSignature,inetSocketAddress);
    }

    @Test
    public  void addChildrenListener(){
        CuratorCache curatorCache = CuratorCache.build(CuratorUtils.getZkClient(), "/ya-rpc-provider/com.wang.HelloService?group=1&version=1");
        curatorCache.listenable().addListener((type, oldData, newData) -> {
            switch (type){
                case NODE_CREATED:
                    System.out.println("zNode created, newpath: " + newData.getPath());
                    break;
                case NODE_CHANGED:
                    System.out.println("zNode changed, oldPath: "+ oldData.getPath() +"newPath: " + newData.getPath());
                    break;
                case NODE_DELETED:
                    System.out.println("zNode deleted, oldPath: "+ oldData.getPath());
                    break;
                default:
                    System.out.println("Unknown");
            }
        });
        curatorCache.start();
        System.out.println("watch start");
        ZookeeperRegistry registry = new ZookeeperRegistry();
        ServiceSignature serviceSignature = new ServiceSignature("com.wang.HelloService","1","1");
        InetSocketAddress inetSocketAddress = new InetSocketAddress("2.3.4.5",8080);
        registry.register(serviceSignature,inetSocketAddress);
        registry.unregister(serviceSignature,inetSocketAddress);
        System.out.println("watch stop");
        curatorCache.close();
    }

}
