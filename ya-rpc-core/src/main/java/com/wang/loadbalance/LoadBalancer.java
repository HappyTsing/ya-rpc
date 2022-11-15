package com.wang.loadbalance;

import com.wang.extension.SPI;

import java.util.List;

/**
 * 负责均衡
 * @author happytsing
 */
@SPI
public interface LoadBalancer {
    /**
     *
     * @param providerList
     * @return
     */
    String selectProvider (List<String> providerList);
}
