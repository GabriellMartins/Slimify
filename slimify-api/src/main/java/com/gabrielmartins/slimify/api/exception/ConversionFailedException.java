package com.gabrielmartins.slimify.api.exception;

import lombok.Getter;

@Getter
public class ConversionFailedException extends SlimifyException {

    private final String world;

    public ConversionFailedException(String world, String message) {
        super(message);
        this.world = world;
    }

    public ConversionFailedException(String world, String message, Throwable cause) {
        super(message, cause);
        this.world = world;
    }
}
