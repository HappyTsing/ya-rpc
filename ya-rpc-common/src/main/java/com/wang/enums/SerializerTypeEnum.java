package com.wang.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author happytsing
 */
@AllArgsConstructor
@Getter
public enum SerializerTypeEnum {

    PROTOSTUFF((byte) 0x01, "protostuff"),
    HESSIAN((byte) 0X02, "hessian");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializerTypeEnum c : SerializerTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

}
