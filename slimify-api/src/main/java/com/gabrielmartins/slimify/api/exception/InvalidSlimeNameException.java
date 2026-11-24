package com.gabrielmartins.slimify.api.exception;

import lombok.Getter;

@Getter
public class InvalidSlimeNameException extends SlimifyException {

    private final String name;

    public InvalidSlimeNameException(String name) {
        super("Invalid slime name: " + name);
        this.name = name;
    }
}
