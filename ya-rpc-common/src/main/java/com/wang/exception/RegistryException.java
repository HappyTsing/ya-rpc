package com.wang.exception;

/**
 * @author happytsing
 */
public class RegistryException extends RuntimeException {

    public RegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistryException(Throwable cause) {
        super(cause);
    }

    public RegistryException(String message) {
        super(message);
    }
}
