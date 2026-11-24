package com.gabrielmartins.slimify.api;

import java.nio.file.Path;

import com.gabrielmartins.slimify.api.event.ConversionListener;
import com.gabrielmartins.slimify.api.option.SlimifyOptions;
import com.gabrielmartins.slimify.api.result.ConversionReport;
import com.gabrielmartins.slimify.api.result.ConversionResult;
import com.gabrielmartins.slimify.api.storage.SlimeStorage;
import com.gabrielmartins.slimify.api.world.WorldInspector;

public interface Slimify {

    SlimifyOptions options();

    SlimeStorage storage();

    WorldInspector inspector();

    ConversionReport convertAll();

    ConversionResult convert(Path world);

    ConversionResult convert(Path world, String slimeName);

    void addListener(ConversionListener listener);

    void removeListener(ConversionListener listener);
}
