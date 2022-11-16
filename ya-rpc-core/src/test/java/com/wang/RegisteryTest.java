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
    public void unregisterAllService(){
        ZookeeperRegistry registry = (ZookeeperRegistry) ExtensionLoader.getExtensionLoader(Registry.class).getExtension("zookeeper");
        registry.unregisterAllService();
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

}
