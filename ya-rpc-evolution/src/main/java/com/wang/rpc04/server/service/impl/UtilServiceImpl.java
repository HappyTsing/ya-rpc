package com.wang.rpc04.server.service.impl;

import com.wang.api.service.UtilService;

/**
 * @author happytsing
 */
public class UtilServiceImpl implements UtilService {
    @Override
    public float sum(float a, float b) {
        return a+b;
    }

    @Override
    public String uppercase(String str) {
        return str.toUpperCase();
    }
}
