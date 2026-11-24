package com.gabrielmartins.slimify.importer.engine;

import java.util.Arrays;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OutputParser {

    private final Pattern ANSI = Pattern.compile("\u001B\\[[;\\d]*m");
    private final String FALLBACK = "The converter produced no file";

    public String reason(String output) {
        return Arrays.stream(ANSI.matcher(output).replaceAll("").split("\\R"))
                .map(String::strip)
                .filter(line -> !line.isEmpty() && !line.startsWith("at "))
                .reduce((first, second) -> second)
                .orElse(FALLBACK);
    }
}
