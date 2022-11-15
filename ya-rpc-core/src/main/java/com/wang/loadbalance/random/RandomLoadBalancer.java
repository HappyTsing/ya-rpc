package com.wang.loadbalance.random;

import com.wang.loadbalance.AbstractLoadBalancer;
import java.util.List;
import java.util.Random;

/**
 * 随机策略
 * @author happytsing
 */
public class RandomLoadBalancer extends AbstractLoadBalancer {

    @Override
    public String doSelect(List<String> providerList) {
        Random random = new Random();
        return providerList.get(random.nextInt(providerList.size()));
    }
}

