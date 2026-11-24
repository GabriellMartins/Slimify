package com.gabrielmartins.slimify.api.event;

import java.nio.file.Path;

import com.gabrielmartins.slimify.api.result.ConversionResult;

public interface ConversionListener {

    default void started(String world, Path source, Path target) {
    }

    default void finished(ConversionResult result) {
    }
}
