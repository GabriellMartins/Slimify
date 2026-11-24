package com.gabrielmartins.slimify.importer.engine;

import java.nio.file.Path;

import com.gabrielmartins.slimify.api.exception.ConversionFailedException;

public interface ConversionEngine {

    void convert(Path world, Path target) throws ConversionFailedException;
}
