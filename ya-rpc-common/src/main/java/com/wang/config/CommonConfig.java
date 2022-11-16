package com.wang.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonConfig {

    /**
     * 默认配置
     */
    private static final String DEFAULT_SERIALIZER = "protostuff";
    private static final String DEFAULT_COMPRESSOR = "gzip";
    private static final String DEFAULT_REGISTRY = "zookeeper";
    private static final String DEFAULT_LOADBALANCER = "roundrobin";

    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    private static final int DEFAULT_RPC_SERVER_PORT = 8713;
    private static final int RETRY = 3;



    /**
     * 自定义配置
     *      clientSerializer 客户端使用的序列化器
     *                       服务端收到数据后，Header 中包含了 1 个字节的 SerializerType
     *                       其用于说明客户端使用的是哪种序列化器对 RpcRequest 进行序列化
     *                       此时，服务端使用相同的序列化器对数据进行反序列化。
     *      serverSerializer 服务端序列化器
     *                       服务端处理请求后，需要响应 RpcResponse，此时需要对其进行序列化。
     *      clientCompressor 客户端压缩器
     *                       与序列化器相同，Header 中包含了 1 个字节的 CompressorType，其用于数模客户端使用哪种压缩器
     *                       此时，服务端使用相同的压缩器对数据进行解压缩。
     *      serverCompressor 服务端压缩器
     *      registry         注册中心
     *      loadbalancer     负载均衡器
     *      zookeeperAddress zookeeper地址，一般是集群的形式
     *      rpcServerPort    服务端响应 RPC 请求的端口号
     *      retry            超时重传次数
     */
    public static String clientSerializer = DEFAULT_SERIALIZER;
    public static String serverSerializer = DEFAULT_SERIALIZER;

    public static String clientCompressor = DEFAULT_COMPRESSOR;
    public static String serverCompressor = DEFAULT_COMPRESSOR;

    public static String registry = DEFAULT_REGISTRY;
    public static String loadbalancer = DEFAULT_LOADBALANCER;

    public static String zookeeperAddress =  "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
    public static int rpcServerPort =  DEFAULT_RPC_SERVER_PORT;
    public static int retry =  RETRY;



}
