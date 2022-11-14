package com.wang.registry.zookeeper;

import com.wang.dto.RpcRequest;
import com.wang.dto.ServiceSignature;
import com.wang.extension.ExtensionLoader;
import com.wang.loadbalance.LoadBalancer;
import com.wang.registry.Registry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;


import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * servicePath: 服务的路径，该路径下就是多个提供该服务的服务器
 * providerPath: 提供服务的服务器ip:port 对应的 znode 的路径
 * provider: ip:port 提供服务的服务器地址和端口
 *
 */
@Slf4j
public class ZookeeperRegistry implements Registry {

    private final CuratorFramework zkClient;
    private final LoadBalancer loadBalancer;
    public static final Path ZK_REGISTER_ROOT_PATH = Paths.get("/ya-rpc-provider");

    /**
     * key: ServiceSignature
     * value: Provider
     */
    private static final Map<ServiceSignature, List<String>> PROVIDER_CACHE = new ConcurrentHashMap<>();


    /**
     * 已经注册的 providerPath
     */
    private static final Set<Path> REGISTERED_PROVIDER_PATH_CACHE = ConcurrentHashMap.newKeySet();

    public ZookeeperRegistry() {
        this.zkClient = CuratorUtils.getZkClient();
        this.loadBalancer = ExtensionLoader.getExtensionLoader(LoadBalancer.class).getExtension("roundrobin");
    }



    @Override
    public void register(ServiceSignature serviceSignature, InetSocketAddress inetSocketAddress) {
        Path providerPath = ZK_REGISTER_ROOT_PATH.resolve(serviceSignature.toString()).resolve(toZnodeName(inetSocketAddress));
        if(REGISTERED_PROVIDER_PATH_CACHE.contains(providerPath)){
            log.info("The service already registered. The providerPath is [{}]",providerPath);
        }else{
            CuratorUtils.createPersistentNode(zkClient,providerPath);
            log.info("Register service success, The providerPath is [{}]",providerPath);
        }
    }

    @Override
    public void unregister(ServiceSignature serviceSignature, InetSocketAddress inetSocketAddress) {
        Path providerPath = ZK_REGISTER_ROOT_PATH.resolve(serviceSignature.toString()).resolve(toZnodeName(inetSocketAddress));
        CuratorUtils.removeNode(zkClient,providerPath,false);
    }

    @Override
    public void unregisterAllMyService() {
        CuratorUtils.removeNode(zkClient,ZK_REGISTER_ROOT_PATH,true);
    }

    @Override
    public InetSocketAddress lookup(RpcRequest rpcRequest) {
        List<String> providerList = null;
        ServiceSignature serviceSignature = rpcRequest.getServiceSignature();
        if (PROVIDER_CACHE.containsKey(serviceSignature)) {
            providerList =  PROVIDER_CACHE.get(serviceSignature);
            log.info("Lookup service success, hit provider cache. The providerList is [{}]", providerList);
        }else {
            Path servicePath = ZK_REGISTER_ROOT_PATH.resolve(serviceSignature.toString());
            providerList = CuratorUtils.getChildrenNodes(zkClient,servicePath);
            PROVIDER_CACHE.put(serviceSignature,providerList);
            log.info("Lookup service success, not hit provider cache, get from zookeeper. The providerList is [{}]", providerList);
        }
        watch(serviceSignature);

        // 每个服务下面有若干个 znode 节点，每个节点的名字就是服务的地址。
        // 例如服务 com.wang.HelloService?group=1&version=1
        // 这个服务下面有两个子节点：192.168.0.1:2020 和 192.168.0.2：2:2021
        // 这两个服务器都可以提供此服务，此时就需要使用负载均衡，从中选取一个服务。
        String selectedProvider = loadBalancer.selectProvider(providerList);
        log.info("SelectedProvider is [{}]", selectedProvider);
        String[] hostIp = selectedProvider.split(":");
        String host = hostIp[0];
        int port = Integer.parseInt(hostIp[1]);
        return new InetSocketAddress(host, port);
    }

    @Override
    public void watch(ServiceSignature serviceSignature) {
        Path servicePath = ZK_REGISTER_ROOT_PATH.resolve(serviceSignature.toString());
        log.info("Watch service success, Watching service is [{}]",serviceSignature);
        CuratorUtils.addChildrenListener(zkClient,servicePath, (type, oldData, newData) -> {
            log.info("type: {}, oldData: {}, newData: {}", type, oldData, newData);
            List<String> providerList = CuratorUtils.getChildrenNodes(zkClient,servicePath);
            PROVIDER_CACHE.put(serviceSignature,providerList);
//            根据监听到的节点的动作做不同处理。但此处无论子节点（此处的子节点是 provider）如何变化，都重新获取所有子节点，更新缓存。
//            switch (type){
//                case NODE_CREATED:
//                    log.info("zNode created, newpath: " + newData.getPath());
//                    break;
//                case NODE_CHANGED:
//                    log.info("zNode changed, oldPath: {}, newPath: {}",oldData.getPath(),newData.getPath());
//                    break;
//                case NODE_DELETED:
//                    log.info("zNode deleted, oldPath: "+ oldData.getPath());
//                    break;
//                default:
//                    log.info("Unknown");
//            }
        });
    }

    private String toZnodeName(InetSocketAddress inetSocketAddress){

        // 如果 host 是域名时，例如 www.baidu.com，直接 inetSocketAddress.toString() 结果为 www.baidu.com/110.242.68.4:8080
        // 因此将其转换为数值形式
        String host = inetSocketAddress.getAddress().getHostAddress();
        int port = inetSocketAddress.getPort();
        return host+":"+port;

    }
}
