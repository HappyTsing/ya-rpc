package com.wang;

import com.wang.dto.ServiceSignature;
import org.junit.Test;

public class ServiceSignatureTest {
    @Test
    public void test(){
        ServiceSignature servicePath = new ServiceSignature("com.wang.HelloService","1","1");
        ServiceSignature servicePath2 = new ServiceSignature("com.wang.HelloService","1","1");
        System.out.println(servicePath2.equals(servicePath));
        System.out.println(servicePath.hashCode());
        System.out.println(servicePath2.hashCode());
    }
}
