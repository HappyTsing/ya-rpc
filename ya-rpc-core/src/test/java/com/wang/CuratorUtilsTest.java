package com.wang;

import com.wang.dto.ServiceSignature;
import com.wang.registry.zookeeper.CuratorUtils;
import com.wang.registry.zookeeper.ZookeeperRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.List;

public class CuratorUtilsTest {
    @Test
    public void getclient(){
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(zkClient, Paths.get("/"));
        System.out.println(childrenNodes);
    }

    @Test
    public void removeNode(){
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.removeNode(zkClient, Paths.get("/ya-rpc-provider"),true);
    }

    @Test
    public void createNode(){
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient,Paths.get("/ya-rpc-provider/com.wang.HelloService/127.0.0.1:9999"));
        CuratorUtils.createPersistentNode(zkClient,Paths.get("/ya-rpc-provider/com.wang.HelloService/127.0.0.2:8888"));
    }

    @Test
    public void getChildrenNodes(){
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.getChildrenNodes(zkClient,Paths.get("/ya-rpc-provider/com.wang.HelloService"));
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
