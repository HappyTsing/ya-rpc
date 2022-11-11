package com.wang.extension;

import java.lang.annotation.*;

/**
 * 标记扩展接口
 * @author happytsing
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
