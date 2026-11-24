package com.gabrielmartins.slimify.api.exception;

import java.nio.file.Path;

import lombok.Getter;

@Getter
public class InvalidWorldException extends SlimifyException {

    private final Path path;

    public InvalidWorldException(Path path, String message) {
        super(message);
        this.path = path;
    }
}
