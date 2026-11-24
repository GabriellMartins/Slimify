package com.gabrielmartins.slimify.importer.naming;

import java.util.regex.Pattern;

import com.gabrielmartins.slimify.api.exception.InvalidSlimeNameException;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SlimeNames {

    private final Pattern VALID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");

    public boolean isValid(String name) {
        return name != null && VALID.matcher(name).matches();
    }

    public String require(String name) {
        if (!isValid(name)) {
            throw new InvalidSlimeNameException(name);
        }
        return name;
    }
}
