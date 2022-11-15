package com.wang.loadbalance;

import java.util.List;

public abstract class AbstractLoadBalancer implements LoadBalancer{
    @Override
    public String selectProvider(List<String> providerList) {
        if( providerList == null || providerList.isEmpty()){
            return null;
        }

        if(providerList.size() == 1){
            return providerList.get(0);
        }
        return doSelect(providerList);
    }

    protected abstract String doSelect(List<String> providerList);

}
