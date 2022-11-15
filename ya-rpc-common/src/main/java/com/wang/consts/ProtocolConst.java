package com.wang.consts;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 协议常量
 * @author happytsing
 */
public class ProtocolConst {

    public static final byte[] MAGIC_CODE = {(byte)0x4A, (byte)0x2e, (byte)0x32, (byte)0x01};
    public static final byte VERSION = 1;
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final int MAGIC_LENGTH = 4;
    public static final int VERSION_LENGTH = 1;
    public static final int BODY_LENGTH_LENGTH = 4;
    public static final int MESSAGE_TYPE_LENGTH = 1;
    public static final int COMPRESSOR_TYPE_LENGTH = 1;
    public static final int SERIALIZER_TYPE_LENGTH = 1;

    public static final int HEADER_LENGTH = MAGIC_LENGTH + VERSION_LENGTH
            + BODY_LENGTH_LENGTH + MESSAGE_TYPE_LENGTH
            + COMPRESSOR_TYPE_LENGTH + SERIALIZER_TYPE_LENGTH;

}
