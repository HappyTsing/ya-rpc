package com.wang;

import com.wang.compress.Compressor;
import com.wang.compress.gzip.GzipCompressor;
import com.wang.serialize.Serializer;
import com.wang.serialize.hessian.HessianSerializer;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompressTest {
    /**
     * TODO 为啥压缩之后变得更大了？
     */
    @Test
    public void gzip(){
        Serializer serializer = new HessianSerializer();
        Compressor compress = new GzipCompressor();
        HashMap<String,String> testMap = new HashMap<>();
        testMap.put("key", "value");
        byte[]  serializeBytes = serializer.serialize(testMap);
        byte[] compressBytes = compress.compress(serializeBytes);
        byte[] decompressBytes = compress.decompress(compressBytes);
        System.out.println(serializeBytes.length);
        System.out.println(compressBytes.length);
        assertEquals(serializeBytes.length,decompressBytes.length);
    }
}
