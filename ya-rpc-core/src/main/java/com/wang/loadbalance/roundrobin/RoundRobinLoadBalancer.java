package com.wang.loadbalance.roundrobin;

import com.wang.loadbalance.AbstractLoadBalancer;
import java.util.List;

/**
 * 轮询策略
 * @author happytsing
 */
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {
    private static long pos = 0;

    @Override
    public java.lang.String doSelect(List<java.lang.String> providerList) {
        int index = (int) (pos % providerList.size());
        pos++;
        return providerList.get(index);
    }
}
