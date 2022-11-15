package com.wang.serviceImpl;


import com.wang.service.UtilService;

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
