package com.gabrielmartins.slimify.importer;

import java.nio.file.Path;

import com.gabrielmartins.slimify.api.Slimify;
import com.gabrielmartins.slimify.api.event.ConversionListener;
import com.gabrielmartins.slimify.api.option.SlimifyOptions;
import com.gabrielmartins.slimify.api.result.ConversionReport;
import com.gabrielmartins.slimify.api.result.ConversionResult;
import com.gabrielmartins.slimify.api.storage.SlimeStorage;
import com.gabrielmartins.slimify.api.world.WorldInspector;
import com.gabrielmartins.slimify.importer.engine.ConversionEngine;
import com.gabrielmartins.slimify.importer.engine.LegacyEngine;
import com.gabrielmartins.slimify.importer.event.ListenerRegistry;
import com.gabrielmartins.slimify.importer.pipeline.ConversionPipeline;
import com.gabrielmartins.slimify.importer.storage.FileSlimeStorage;
import com.gabrielmartins.slimify.importer.world.AnvilInspector;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SlimeImporter implements Slimify {

    private final SlimifyOptions options;
    private final SlimeStorage storage;
    private final WorldInspector inspector;
    @Getter(AccessLevel.NONE)
    private final ListenerRegistry listeners;
    @Getter(AccessLevel.NONE)
    private final ConversionPipeline pipeline;

    public static Slimify create(SlimifyOptions options) {
        return create(options, new LegacyEngine());
    }

    public static Slimify create(SlimifyOptions options, ConversionEngine engine) {
        SlimeStorage storage = new FileSlimeStorage(options.outputDirectory());
        WorldInspector inspector = new AnvilInspector();
        ListenerRegistry listeners = new ListenerRegistry();
        return new SlimeImporter(options, storage, inspector, listeners, new ConversionPipeline(options, engine, inspector, storage, listeners));
    }

    @Override
    public ConversionReport convertAll() {
        return new ConversionReport(inspector.scan(options.getRoot(), options.getMatcher()).stream().map(this::convert).toList());
    }

    @Override
    public ConversionResult convert(Path world) {
        return convert(world, options.getNamer().name(world.getFileName().toString()));
    }

    @Override
    public ConversionResult convert(Path world, String slimeName) {
        return pipeline.run(world, slimeName);
    }

    @Override
    public void addListener(ConversionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ConversionListener listener) {
        listeners.remove(listener);
    }
}
