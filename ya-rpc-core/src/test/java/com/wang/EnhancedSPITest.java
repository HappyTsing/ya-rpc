package com.wang;

import com.wang.extension.ExtensionLoader;
import com.wang.serialize.Serializer;
import org.junit.Test;

import java.util.HashMap;

public class EnhancedSPITest {
    @Test
    public void protostuff(){
        ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
        Serializer serializer = extensionLoader.getExtension("protostuff");
        String str = "hello world";
        byte[] bytes = serializer.serialize(str);
        String str2 = serializer.deserialize(bytes,String.class);
        System.out.println(str2);
    }
    @Test
    public void hessian(){
        ExtensionLoader<Serializer> extensionLoader = ExtensionLoader.getExtensionLoader(Serializer.class);
        Serializer serializer = extensionLoader.getExtension("hessian");
        HashMap<String,String> testMap = new HashMap<>();
        testMap.put("key", "value");
        System.out.println(testMap);
        byte[]  bytes = serializer.serialize(testMap);
        HashMap sMap = serializer.deserialize(bytes,HashMap.class);
        System.out.println(sMap);
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
