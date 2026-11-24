package com.gabrielmartins.slimify.importer.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gabrielmartins.slimify.api.exception.InvalidSlimeNameException;
import com.gabrielmartins.slimify.api.storage.SlimeFile;
import com.gabrielmartins.slimify.importer.WorkDirTest;

class FileSlimeStorageTest extends WorkDirTest {

    private FileSlimeStorage storage;

    @BeforeEach
    void createStorage() {
        storage = new FileSlimeStorage(root.resolve("slime_worlds"));
    }

    private void write(String fileName, int size) throws IOException {
        Files.createDirectories(storage.directory());
        Files.write(storage.directory().resolve(fileName), new byte[size]);
    }

    @Test
    void missingDirectoryListsNothing() {
        assertTrue(storage.list().isEmpty());
    }

    @Test
    void listIsSortedAndOnlyHasSlimeFiles() throws IOException {
        write("b.slime", 20);
        write("a.slime", 10);
        write("notes.txt", 5);
        Files.createDirectories(storage.directory().resolve("folder.slime"));

        var files = storage.list();

        assertEquals(java.util.List.of("a", "b"), files.stream().map(SlimeFile::name).toList());
        assertEquals(10, files.get(0).sizeBytes());
        assertEquals(storage.directory().resolve("a.slime"), files.get(0).path());
    }

    @Test
    void existsAndLocate() throws IOException {
        write("world.slime", 1);

        assertTrue(storage.exists("world"));
        assertFalse(storage.exists("other"));
        assertEquals(storage.directory().resolve("world.slime"), storage.locate("world"));
    }

    @Test
    void namesCannotEscapeTheDirectory() {
        assertThrows(InvalidSlimeNameException.class, () -> storage.locate("../evil"));
        assertThrows(InvalidSlimeNameException.class, () -> storage.exists("a/b"));
        assertThrows(InvalidSlimeNameException.class, () -> storage.delete(""));
    }

    @Test
    void deleteReportsWhetherSomethingWasRemoved() throws IOException {
        write("world.slime", 1);

        assertTrue(storage.delete("world"));
        assertFalse(storage.delete("world"));
        assertFalse(Files.exists(Path.of(storage.directory().toString(), "world.slime")));
    }
}
