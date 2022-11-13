package com.wang.loadbalance;

import java.util.List;

public interface LoadBalancer {
    /**
     *
     * @param providerList
     * @return
     */
    String selectProvider (List<String> providerList);
}
