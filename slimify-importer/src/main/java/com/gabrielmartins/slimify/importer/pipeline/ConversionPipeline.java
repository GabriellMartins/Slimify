package com.gabrielmartins.slimify.importer.pipeline;

import java.nio.file.Path;
import java.util.Optional;

import com.gabrielmartins.slimify.api.exception.ConversionFailedException;
import com.gabrielmartins.slimify.api.exception.SlimifyException;
import com.gabrielmartins.slimify.api.option.SlimifyOptions;
import com.gabrielmartins.slimify.api.result.ConversionResult;
import com.gabrielmartins.slimify.api.storage.SlimeStorage;
import com.gabrielmartins.slimify.api.world.WorldInfo;
import com.gabrielmartins.slimify.api.world.WorldInspector;
import com.gabrielmartins.slimify.importer.engine.ConversionEngine;
import com.gabrielmartins.slimify.importer.event.ListenerRegistry;
import com.gabrielmartins.slimify.importer.format.SlimeHeaderReader;
import com.gabrielmartins.slimify.importer.naming.SlimeNames;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ConversionPipeline {

    private final SlimifyOptions options;
    private final ConversionEngine engine;
    private final WorldInspector inspector;
    private final SlimeStorage storage;
    private final ListenerRegistry listeners;

    public ConversionResult run(Path world, String slimeName) {
        String folder = String.valueOf(world.getFileName());
        long start = System.nanoTime();
        ConversionResult result = execute(world, folder, slimeName, start);
        listeners.finished(result);
        return result;
    }

    private ConversionResult execute(Path world, String folder, String slimeName, long start) {
        Path target = null;
        try {
            String name = SlimeNames.require(slimeName);
            target = storage.locate(name);
            WorldInfo info = inspector.inspect(world);
            Optional<String> violation = options.getLimits().violation(info);
            if (violation.isPresent()) {
                throw new ConversionFailedException(folder, violation.get());
            }
            if (storage.exists(name)) {
                switch (options.getConflictPolicy()) {
                    case SKIP -> {
                        return ConversionResult.skipped(folder, world, target, "Target already exists");
                    }
                    case FAIL -> throw new ConversionFailedException(folder, "Target already exists: " + target.getFileName());
                    case OVERWRITE -> {
                    }
                }
            }
            listeners.started(folder, world, target);
            engine.convert(world, target);
            if (SlimeHeaderReader.read(target).isEmpty()) {
                storage.delete(name);
                throw new ConversionFailedException(folder, "The converter produced an invalid slime file");
            }
            return ConversionResult.converted(folder, world, target, elapsedSince(start));
        } catch (SlimifyException e) {
            return ConversionResult.failed(folder, world, target, elapsedSince(start), e.getMessage());
        } catch (RuntimeException e) {
            return ConversionResult.failed(folder, world, target, elapsedSince(start), String.valueOf(e.getMessage()));
        }
    }

    private static long elapsedSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
