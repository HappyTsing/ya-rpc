package com.wang.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author happytsing
 */

@AllArgsConstructor
@Getter
public enum MessageTypeEnum {
    REQUEST((byte) 0x01, "request"),

    RESPONSE((byte) 0x02, "response"),;

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (MessageTypeEnum c : MessageTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }



}
