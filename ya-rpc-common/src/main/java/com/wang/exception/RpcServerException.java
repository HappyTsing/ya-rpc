package com.wang.exception;

/**
 * @author happytsing
 */
public class RpcServerException extends RuntimeException {

    public RpcServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcServerException(Throwable cause) {
        super(cause);
    }

    public RpcServerException(String message) {
        super(message);
    }

}
