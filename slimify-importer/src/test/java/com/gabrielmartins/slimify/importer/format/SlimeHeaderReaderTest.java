package com.gabrielmartins.slimify.importer.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.gabrielmartins.slimify.api.format.SlimeHeader;
import com.gabrielmartins.slimify.importer.WorkDirTest;

class SlimeHeaderReaderTest extends WorkDirTest {

    private Path file(byte... bytes) throws IOException {
        Path file = root.resolve("test.slime");
        Files.write(file, bytes);
        return file;
    }

    @Test
    void readsTheVersionOfAValidFile() throws IOException {
        assertEquals(new SlimeHeader(9), SlimeHeaderReader.read(file((byte) 0xB1, (byte) 0x0B, (byte) 9, (byte) 1)).orElseThrow());
    }

    @Test
    void wrongMagicIsNotASlimeFile() throws IOException {
        assertTrue(SlimeHeaderReader.read(file((byte) 0xB1, (byte) 0x0C, (byte) 9)).isEmpty());
    }

    @Test
    void shortFilesAreNotSlimeFiles() throws IOException {
        assertTrue(SlimeHeaderReader.read(file((byte) 0xB1, (byte) 0x0B)).isEmpty());
        assertTrue(SlimeHeaderReader.read(file()).isEmpty());
    }

    @Test
    void missingFilesAreNotSlimeFiles() {
        assertTrue(SlimeHeaderReader.read(root.resolve("missing.slime")).isEmpty());
    }
}
