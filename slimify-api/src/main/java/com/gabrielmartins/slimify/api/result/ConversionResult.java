package com.gabrielmartins.slimify.api.result;

import java.nio.file.Path;

public record ConversionResult(String world, Path source, Path target, ConversionStatus status, long elapsedMillis, String detail) {

    public static ConversionResult converted(String world, Path source, Path target, long elapsedMillis) {
        return new ConversionResult(world, source, target, ConversionStatus.CONVERTED, elapsedMillis, "");
    }

    public static ConversionResult skipped(String world, Path source, Path target, String detail) {
        return new ConversionResult(world, source, target, ConversionStatus.SKIPPED, 0, detail);
    }

    public static ConversionResult failed(String world, Path source, Path target, long elapsedMillis, String detail) {
        return new ConversionResult(world, source, target, ConversionStatus.FAILED, elapsedMillis, detail);
    }

    public boolean success() {
        return status != ConversionStatus.FAILED;
    }
}
