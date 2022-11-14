package com.wang.registry.zookeeper;

import com.wang.exception.RegistryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Slf4j
public class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;


    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";

    /**
     * Create persistent nodes. Unlike temporary nodes, persistent nodes are not removed when the client disconnects
     * 根据路径创建持久节点
     * @param path node path
     */
    public static void createPersistentNode(CuratorFramework zkClient, Path path) {
        try {
            if (zkClient.checkExists().forPath(path.toString()) != null) {
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                //eg: /ya-rpc/com.wang.HelloService/127.0.0.1:9999
                zkClient
                        //创建节点构建器CreateBuilder
                        .create()
                        //递归创建
                        .creatingParentsIfNeeded()
                        //创建节点的类型，这里选的是创建持久化节点。
                        .withMode(CreateMode.PERSISTENT)
                        //创建节点的路径和数据
                        .forPath(path.toString());
                log.info("Create node success. The node is: [{}]", path);
            }
        } catch (Exception e) {
            log.error("Create node fail. The node is: [{}]", path);
            throw new RegistryException("Create node fail.",e);
        }
    }

    /**
     *
     * @param zkClient
     * @param path
     * @param recursive 递归删除
     */
    public static void removeNode(CuratorFramework zkClient, Path path, Boolean recursive) {
        try{
            if(recursive){
                zkClient.delete().deletingChildrenIfNeeded().forPath(path.toString());
            }else {
                zkClient.delete().forPath(path.toString());
            }
            log.info("Delete node success. The node is [{}]",path);
        } catch (Exception e) {
            log.info("Delete node fail. The node is [{}]",path);
            throw new RegistryException("Delete node fail.",e);
        }
    }

    /**
     * Gets the children under a node
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, Path path) {

        try {
            List<String> result = zkClient.getChildren().forPath(path.toString());
            log.info("Get children success. The children are [{}]",result);
            return result;
        } catch (Exception e) {
            log.error("Get children nodes fail. The father node is: [{}]", path);
            throw new RegistryException("Get children nodes fail.",e);
        }
    }

    public static CuratorFramework getZkClient() {
        // check if user has set zk address
        // todo 修改为从配置读入
        String zookeeperAddress = DEFAULT_ZOOKEEPER_ADDRESS;
        // if zkClient has been started, return directly
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                // 设置Zookeeper服务器地址，多个用逗号分隔
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // wait 30s until connect to the zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RegistryException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            throw new RegistryException(e);
        }
        return zkClient;
    }


    public static void addChildrenListener(CuratorFramework zkClient, Path path, CuratorCacheListener curatorCacheChildrenListener){
        CuratorCache curatorCache = CuratorCache.build(zkClient, path.toString());
        curatorCache.listenable().addListener(curatorCacheChildrenListener);
        curatorCache.start();
    }
}
