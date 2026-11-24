package com.gabrielmartins.slimify.importer.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gabrielmartins.slimify.api.exception.InvalidWorldException;
import com.gabrielmartins.slimify.api.option.WorldMatcher;
import com.gabrielmartins.slimify.api.world.WorldInfo;
import com.gabrielmartins.slimify.importer.WorkDirTest;
import com.gabrielmartins.slimify.importer.WorldFixture;

class AnvilInspectorTest extends WorkDirTest {

    private final AnvilInspector inspector = new AnvilInspector();

    @Test
    void worldNeedsLevelFileAndRegionFolder() throws IOException {
        Path world = WorldFixture.create(root, "world");
        Path noRegion = Files.createDirectories(root.resolve("no_region"));
        Files.write(noRegion.resolve("level.dat"), new byte[1]);
        Path noLevel = Files.createDirectories(root.resolve("no_level"));
        Files.createDirectories(noLevel.resolve("region"));

        assertTrue(inspector.isWorld(world));
        assertFalse(inspector.isWorld(noRegion));
        assertFalse(inspector.isWorld(noLevel));
        assertFalse(inspector.isWorld(root.resolve("missing")));
    }

    @Test
    void inspectCountsChunksRegionsAndSize() throws IOException {
        WorldInfo info = inspector.inspect(WorldFixture.create(root, "world"));

        assertEquals("world", info.name());
        assertEquals(1, info.regionFiles());
        assertEquals(1, info.chunks());
        assertTrue(info.sizeBytes() >= 8192);
        assertFalse(info.empty());
    }

    @Test
    void emptyRegionsHaveNoChunks() throws IOException {
        WorldInfo info = inspector.inspect(WorldFixture.empty(root, "void"));

        assertEquals(0, info.chunks());
        assertTrue(info.empty());
    }

    @Test
    void inspectRejectsFoldersThatAreNotWorlds() throws IOException {
        Path plain = Files.createDirectories(root.resolve("plain"));

        InvalidWorldException error = assertThrows(InvalidWorldException.class, () -> inspector.inspect(plain));

        assertEquals(plain, error.getPath());
    }

    @Test
    void scanReturnsMatchingWorldsInOrder() throws IOException {
        WorldFixture.create(root, "world_nether");
        WorldFixture.create(root, "world");
        WorldFixture.create(root, "lobby");
        Files.createDirectories(root.resolve("world_notes"));

        List<Path> found = inspector.scan(root, WorldMatcher.containing("world"));

        assertEquals(List.of("world", "world_nether"), found.stream().map(path -> path.getFileName().toString()).toList());
        assertEquals(3, inspector.scan(root, WorldMatcher.any()).size());
    }

    @Test
    void scanOfAMissingFolderIsEmpty() {
        assertTrue(inspector.scan(root.resolve("missing"), WorldMatcher.any()).isEmpty());
    }
}
