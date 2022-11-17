package com.wang.serviceImpl.groupName1.version1;


import com.wang.service.UtilService;

import java.math.BigDecimal;

/**
 * @author happytsing
 * @group groupName1
 * @version version1
 */
public class UtilServiceImpl implements UtilService {
    @Override
    public float sum(float a, float b) {
        BigDecimal a1 = new BigDecimal(Float.toString(a));
        BigDecimal b1 = new BigDecimal(Float.toString(b));
        return (float) a1.add(b1).doubleValue();
    }

    @Override
    public String uppercase(String str) {
        return str.toUpperCase();
    }
}
