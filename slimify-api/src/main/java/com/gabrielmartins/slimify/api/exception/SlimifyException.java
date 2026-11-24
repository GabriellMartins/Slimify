package com.gabrielmartins.slimify.api.exception;

public class SlimifyException extends RuntimeException {

    public SlimifyException(String message) {
        super(message);
    }

    public SlimifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
