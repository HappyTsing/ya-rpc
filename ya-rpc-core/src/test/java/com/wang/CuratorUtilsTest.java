package com.wang;

import com.wang.registry.zookeeper.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

public class CuratorUtilsTest {
    @Test
    public void getclient(){
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(zkClient, Paths.get("/"));
        System.out.println(childrenNodes);
    }
}
