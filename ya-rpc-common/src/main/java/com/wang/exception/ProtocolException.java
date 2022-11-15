package com.wang.exception;

/**
 * 协议处理异常，如编码和解码异常
 * @author happytsing
 */
public class ProtocolException extends RuntimeException {

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolException(Throwable cause) {
        super(cause);
    }

    public ProtocolException(String message) {
        super(message);
    }
}
