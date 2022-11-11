package com.wang;

import com.wang.serialize.Serializer;
import org.junit.Test;

import java.util.ServiceLoader;

public class JavaSPITest {
    /**
     * Java 自带的 SPI 机制会实例化所有的功能，因此不太合理，需要对其进行增强。
     * 此外，配置文件约定放置于 META-INF/services 文件夹下
     */
    @Test
    public void test(){
        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
        for (Serializer serializer : serviceLoader) {
            System.out.println(serializer.getClass().getName());
        }
    }
}
