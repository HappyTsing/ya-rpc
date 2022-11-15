package com.wang;

import com.wang.compress.Compressor;
import com.wang.dto.RpcRequest;
import com.wang.extension.ExtensionLoader;
import com.wang.serialize.Serializer;
import org.junit.Test;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * protostuff 效果更好
 * 用 gzip 后可压缩 15% ~ 20%
 */
public class EnhancedSPITest {
    @Test
    public void protostuffAndGzip(){
        ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
        ExtensionLoader<Compressor> compressorLoader = ExtensionLoader.getExtensionLoader(Compressor.class);

        Serializer serializer = extensionLoader.getExtension("protostuff");
        Compressor compressor= compressorLoader.getExtension("gzip");

        RpcRequest target = RpcRequest.builder().methodName("saySomething")
                .params(new Object[]{"Hello","Fuck You"})
                .interfaceName("com.wang.HelloService")
                .paramTypes(new Class<?>[]{String.class, String.class})
                .requestId(UUID.randomUUID().toString())
                .group("1")
                .version("1")
                .build();

        byte[] serializerBytes = serializer.serialize(target);
        System.out.println("protostuff 序列化：" + serializerBytes.length);
        byte[] compressorBytes = compressor.compress(serializerBytes);
        System.out.println("gzip 压缩：" + compressorBytes.length);
        byte[] decompressBytes = compressor.decompress(compressorBytes);
        RpcRequest rpcRequest = serializer.deserialize(decompressBytes,RpcRequest.class);
        System.out.println(rpcRequest);
    }
    @Test
    public void hessianAndGzip(){
        ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
        ExtensionLoader<Compressor> compressorLoader = ExtensionLoader.getExtensionLoader(Compressor.class);

        Serializer serializer = extensionLoader.getExtension("hessian");
        Compressor compressor= compressorLoader.getExtension("gzip");

        RpcRequest target = RpcRequest.builder().methodName("saySomething")
                .params(new Object[]{"Hello","Fuck You"})
                .interfaceName("com.wang.HelloService")
                .paramTypes(new Class<?>[]{String.class, String.class})
                .requestId(UUID.randomUUID().toString())
                .group("1")
                .version("1")
                .build();

        byte[] serializerBytes = serializer.serialize(target);
        System.out.println("hessian 序列化：" + serializerBytes.length);
        byte[] compressorBytes = compressor.compress(serializerBytes);
        System.out.println("gzip 压缩：" + compressorBytes.length);
        byte[] decompressBytes = compressor.decompress(compressorBytes);
        RpcRequest rpcRequest = serializer.deserialize(decompressBytes,RpcRequest.class);
        System.out.println(rpcRequest);
    }

    /**
     * protostuff 序列化 Map、List时无法反序列化
     */
    @Test
    public void protostuffMap(){
        ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
        Serializer serializer = extensionLoader.getExtension("protostuff");
        HashMap<String,String> testMap = new HashMap<>();
        testMap.put("key", "value");
        System.out.println(testMap);
        byte[]  bytes = serializer.serialize(testMap);
        HashMap sMap = serializer.deserialize(bytes,HashMap.class);
        System.out.println(sMap);
    }
}
