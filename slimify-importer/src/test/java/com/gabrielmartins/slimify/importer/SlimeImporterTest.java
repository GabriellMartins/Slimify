package com.gabrielmartins.slimify.importer;

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

import com.gabrielmartins.slimify.api.Slimify;
import com.gabrielmartins.slimify.api.event.ConversionListener;
import com.gabrielmartins.slimify.api.option.ConflictPolicy;
import com.gabrielmartins.slimify.api.option.SlimifyOptions;
import com.gabrielmartins.slimify.api.option.WorldLimits;
import com.gabrielmartins.slimify.api.option.WorldMatcher;
import com.gabrielmartins.slimify.api.option.WorldNamer;
import com.gabrielmartins.slimify.api.result.ConversionReport;
import com.gabrielmartins.slimify.api.result.ConversionResult;
import com.gabrielmartins.slimify.api.result.ConversionStatus;

class SlimeImporterTest extends WorkDirTest {

    Slimify slimify;

    @BeforeEach
    void createSlimify() {
        slimify = SlimeImporter.create(SlimifyOptions.builder().root(root).build());
    }

    @Test
    void convertsAWorldIntoTheOutputFolder() throws IOException {
        Path world = WorldFixture.create(root, "world");

        ConversionResult result = slimify.convert(world);

        assertEquals(ConversionStatus.CONVERTED, result.status(), result.detail());
        Path target = root.resolve("slime_worlds").resolve("world.slime");
        assertEquals(target, result.target());
        byte[] bytes = Files.readAllBytes(target);
        assertEquals((byte) 0xB1, bytes[0]);
        assertEquals((byte) 0x0B, bytes[1]);
        assertTrue(Files.isDirectory(world));
    }

    @Test
    void convertAllTakesOnlyWorldsMatchingTheNameRule() throws IOException {
        WorldFixture.create(root, "world");
        WorldFixture.create(root, "world_nether");
        WorldFixture.create(root, "lobby");
        Files.createDirectories(root.resolve("plugins"));
        Files.createDirectories(root.resolve("world_notes"));

        ConversionReport report = slimify.convertAll();

        assertEquals(List.of("world", "world_nether"), report.results().stream().map(ConversionResult::world).toList());
        assertEquals(2, report.converted(), report.summary());
        assertTrue(Files.exists(root.resolve("slime_worlds/world_nether.slime")));
        assertFalse(Files.exists(root.resolve("slime_worlds/lobby.slime")));
    }

    @Test
    void storageSeesWhatWasConverted() throws IOException {
        WorldFixture.create(root, "world");
        WorldFixture.create(root, "world_nether");

        slimify.convertAll();

        assertEquals(List.of("world", "world_nether"), slimify.storage().list().stream().map(file -> file.name()).toList());
    }

    @Test
    void skipPolicyLeavesTheExistingFile() throws IOException {
        Path world = WorldFixture.create(root, "world");
        slimify.convert(world);

        assertEquals(ConversionStatus.SKIPPED, slimify.convert(world).status());
    }

    @Test
    void overwritePolicyConvertsAgain() throws IOException {
        Path world = WorldFixture.create(root, "world");
        slimify.convert(world);
        Slimify overwriting = SlimeImporter.create(slimify.options().toBuilder().conflictPolicy(ConflictPolicy.OVERWRITE).build());

        assertEquals(ConversionStatus.CONVERTED, overwriting.convert(world).status());
    }

    @Test
    void failPolicyReportsTheConflict() throws IOException {
        Path world = WorldFixture.create(root, "world");
        slimify.convert(world);
        Slimify strict = SlimeImporter.create(slimify.options().toBuilder().conflictPolicy(ConflictPolicy.FAIL).build());

        ConversionResult result = strict.convert(world);

        assertEquals(ConversionStatus.FAILED, result.status());
        assertTrue(result.detail().contains("already exists"));
    }

    @Test
    void namerAndMatcherAreApplied() throws IOException {
        WorldFixture.create(root, "Lobby");
        Slimify custom = SlimeImporter.create(SlimifyOptions.builder()
                .root(root)
                .matcher(WorldMatcher.exactly("Lobby"))
                .namer(folder -> WorldNamer.prefixed("hub_").name(WorldNamer.lowerCase().name(folder)))
                .build());

        ConversionReport report = custom.convertAll();

        assertEquals(1, report.converted(), report.summary());
        assertTrue(Files.exists(root.resolve("slime_worlds/hub_lobby.slime")));
    }

    @Test
    void limitsStopHugeWorldsBeforeConverting() throws IOException {
        Path world = WorldFixture.create(root, "world");
        Slimify limited = SlimeImporter.create(slimify.options().toBuilder().limits(WorldLimits.of(0, 0)).build());
        assertEquals(ConversionStatus.CONVERTED, limited.convert(world).status());

        Slimify tiny = SlimeImporter.create(slimify.options().toBuilder().limits(new WorldLimits(0, 1)).output(root.resolve("other")).build());
        ConversionResult result = tiny.convert(world);

        assertEquals(ConversionStatus.FAILED, result.status());
        assertFalse(Files.exists(root.resolve("other/world.slime")));
    }

    @Test
    void foldersThatAreNotWorldsFail() throws IOException {
        Path plain = Files.createDirectories(root.resolve("world_plain"));

        ConversionResult result = slimify.convert(plain);

        assertEquals(ConversionStatus.FAILED, result.status());
        assertFalse(result.detail().isBlank());
    }

    @Test
    void unsafeNamesAreRejected() throws IOException {
        Path world = WorldFixture.create(root, "world");

        assertEquals(ConversionStatus.FAILED, slimify.convert(world, "../escape").status());
        assertEquals(ConversionStatus.FAILED, slimify.convert(world, "").status());
        assertEquals(ConversionStatus.FAILED, slimify.convert(world, null).status());
        assertFalse(Files.exists(root.resolve("escape.slime")));
    }

    @Test
    void corruptWorldFailsWithAReasonAndLeavesNothingBehind() throws IOException {
        Path world = WorldFixture.corrupt(root, "world_broken");

        ConversionResult result = slimify.convert(world);

        assertEquals(ConversionStatus.FAILED, result.status());
        assertFalse(result.detail().isBlank());
        assertFalse(Files.exists(Path.of("world_broken.slime")));
        assertFalse(Files.exists(root.resolve("slime_worlds/world_broken.slime")));
    }

    @Test
    void failureInOneWorldDoesNotStopTheOthers() throws IOException {
        WorldFixture.corrupt(root, "world_a");
        WorldFixture.create(root, "world_b");

        ConversionReport report = slimify.convertAll();

        assertEquals(List.of(ConversionStatus.FAILED, ConversionStatus.CONVERTED), report.results().stream().map(ConversionResult::status).toList());
    }

    @Test
    void listenersHearAboutEveryConversion() throws IOException {
        List<String> events = new ArrayList<>();
        ConversionListener listener = new ConversionListener() {
            @Override
            public void started(String world, Path source, Path target) {
                events.add("started " + world);
            }

            @Override
            public void finished(ConversionResult result) {
                events.add(result.status() + " " + result.world());
            }
        };
        slimify.addListener(listener);
        Path world = WorldFixture.create(root, "world");

        slimify.convert(world);
        slimify.convert(world);
        slimify.removeListener(listener);
        slimify.convert(world);

        assertEquals(List.of("started world", "CONVERTED world", "SKIPPED world"), events);
    }
}
