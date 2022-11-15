package com.wang.service;

/**
 * @author happytsing
 */
public interface UtilService {
    /**
     * 计算 a 和 b 的合
     * @param a 加数
     * @param b 被加数
     * @return 合
     */
    float sum(float a, float b);

    /**
     * 将字符串变为大写模式
     * @param str 原始字符串
     * @return 大写后的字符串
     */
    String uppercase(String str);

}
