package com.gabrielmartins.slimify.importer.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gabrielmartins.slimify.api.event.ConversionListener;
import com.gabrielmartins.slimify.api.exception.ConversionFailedException;
import com.gabrielmartins.slimify.api.option.ConflictPolicy;
import com.gabrielmartins.slimify.api.option.SlimifyOptions;
import com.gabrielmartins.slimify.api.option.WorldLimits;
import com.gabrielmartins.slimify.api.result.ConversionResult;
import com.gabrielmartins.slimify.api.result.ConversionStatus;
import com.gabrielmartins.slimify.importer.WorkDirTest;
import com.gabrielmartins.slimify.importer.WorldFixture;
import com.gabrielmartins.slimify.importer.engine.ConversionEngine;
import com.gabrielmartins.slimify.importer.event.ListenerRegistry;
import com.gabrielmartins.slimify.importer.storage.FileSlimeStorage;
import com.gabrielmartins.slimify.importer.world.AnvilInspector;

class ConversionPipelineTest extends WorkDirTest {

    private static final byte[] VALID = {(byte) 0xB1, 0x0B, 9, 1, 2, 3};

    private final List<String> calls = new ArrayList<>();
    private final ListenerRegistry listeners = new ListenerRegistry();
    private Path world;
    private ConversionEngine engine;

    @BeforeEach
    void createWorld() throws IOException {
        world = WorldFixture.create(root, "world");
        engine = (source, target) -> {
            calls.add("engine");
            try {
                Files.createDirectories(target.getParent());
                Files.write(target, VALID);
            } catch (IOException e) {
                throw new ConversionFailedException("world", e.getMessage());
            }
        };
        listeners.add(new ConversionListener() {
            @Override
            public void started(String name, Path source, Path target) {
                calls.add("started");
            }

            @Override
            public void finished(ConversionResult result) {
                calls.add("finished " + result.status());
            }
        });
    }

    private ConversionPipeline pipeline(SlimifyOptions options) {
        return new ConversionPipeline(options, engine, new AnvilInspector(), new FileSlimeStorage(options.outputDirectory()), listeners);
    }

    private SlimifyOptions.SlimifyOptionsBuilder options() {
        return SlimifyOptions.builder().root(root);
    }

    @Test
    void successRunsTheEngineBetweenStartAndFinish() {
        ConversionResult result = pipeline(options().build()).run(world, "world");

        assertEquals(ConversionStatus.CONVERTED, result.status());
        assertEquals(List.of("started", "engine", "finished CONVERTED"), calls);
        assertTrue(Files.exists(root.resolve("slime_worlds/world.slime")));
    }

    @Test
    void existingTargetIsSkippedWithoutTouchingTheEngine() throws IOException {
        Files.createDirectories(root.resolve("slime_worlds"));
        Files.write(root.resolve("slime_worlds/world.slime"), VALID);

        ConversionResult result = pipeline(options().build()).run(world, "world");

        assertEquals(ConversionStatus.SKIPPED, result.status());
        assertEquals(List.of("finished SKIPPED"), calls);
    }

    @Test
    void overwriteRunsTheEngineEvenWhenTheTargetExists() throws IOException {
        Files.createDirectories(root.resolve("slime_worlds"));
        Files.write(root.resolve("slime_worlds/world.slime"), VALID);

        ConversionResult result = pipeline(options().conflictPolicy(ConflictPolicy.OVERWRITE).build()).run(world, "world");

        assertEquals(ConversionStatus.CONVERTED, result.status());
        assertTrue(calls.contains("engine"));
    }

    @Test
    void failPolicyFailsWithoutTouchingTheEngine() throws IOException {
        Files.createDirectories(root.resolve("slime_worlds"));
        Files.write(root.resolve("slime_worlds/world.slime"), VALID);

        ConversionResult result = pipeline(options().conflictPolicy(ConflictPolicy.FAIL).build()).run(world, "world");

        assertEquals(ConversionStatus.FAILED, result.status());
        assertFalse(calls.contains("engine"));
    }

    @Test
    void limitsFailBeforeTheEngine() {
        ConversionResult result = pipeline(options().limits(new WorldLimits(0, 1)).build()).run(world, "world");

        assertEquals(ConversionStatus.FAILED, result.status());
        assertTrue(result.detail().contains("limit"));
        assertEquals(List.of("finished FAILED"), calls);
    }

    @Test
    void invalidOutputIsRemovedAndReported() {
        engine = (source, target) -> {
            try {
                Files.createDirectories(target.getParent());
                Files.write(target, new byte[] {1, 2, 3, 4});
            } catch (IOException e) {
                throw new ConversionFailedException("world", e.getMessage());
            }
        };

        ConversionResult result = pipeline(options().build()).run(world, "world");

        assertEquals(ConversionStatus.FAILED, result.status());
        assertTrue(result.detail().contains("invalid slime file"));
        assertFalse(Files.exists(root.resolve("slime_worlds/world.slime")));
    }

    @Test
    void engineFailureBecomesAFailedResult() {
        engine = (source, target) -> {
            throw new ConversionFailedException("world", "boom");
        };

        ConversionResult result = pipeline(options().build()).run(world, "world");

        assertEquals(ConversionStatus.FAILED, result.status());
        assertEquals("boom", result.detail());
    }

    @Test
    void unexpectedEngineErrorsDoNotEscape() {
        engine = (source, target) -> {
            throw new IllegalStateException("surprise");
        };

        ConversionResult result = pipeline(options().build()).run(world, "world");

        assertEquals(ConversionStatus.FAILED, result.status());
        assertEquals("surprise", result.detail());
    }

    @Test
    void invalidNamesNeverReachTheEngine() {
        ConversionResult result = pipeline(options().build()).run(world, "../x");

        assertEquals(ConversionStatus.FAILED, result.status());
        assertFalse(calls.contains("engine"));
        assertEquals(null, result.target());
    }
}
