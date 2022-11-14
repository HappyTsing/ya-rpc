package com.wang.exception;

/**
 * @author happytsing
 */
public class RpcClientException extends RuntimeException {

    public RpcClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcClientException(Throwable cause) {
        super(cause);
    }

    public RpcClientException(String message) {
        super(message);
    }

}
