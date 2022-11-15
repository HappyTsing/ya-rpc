package com.wang.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author happytsing
 */
@AllArgsConstructor
@Getter
public enum CompressorTypeEnum {

    GZIP((byte) 0x01, "gzip"),

    DUMMY((byte) 0x02, "dummy");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressorTypeEnum c : CompressorTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

}
