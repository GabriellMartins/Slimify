package com.gabrielmartins.slimify.importer.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.gabrielmartins.slimify.importer.WorkDirTest;

class RegionReaderTest extends WorkDirTest {

    private Path region(byte[] header) throws IOException {
        Path file = root.resolve("r.0.0.mca");
        Files.write(file, header);
        return file;
    }

    @Test
    void countsOnlyChunksWithAnOffset() throws IOException {
        byte[] header = new byte[4096];
        header[0 * 4 + 2] = 2;
        header[5 * 4 + 1] = 1;
        header[1023 * 4] = 1;

        assertEquals(3, RegionReader.chunkCount(region(header)));
    }

    @Test
    void sectorCountAloneIsNotAChunk() throws IOException {
        byte[] header = new byte[4096];
        header[3] = 5;

        assertEquals(0, RegionReader.chunkCount(region(header)));
    }

    @Test
    void shortFilesDoNotBreakTheReader() throws IOException {
        assertEquals(0, RegionReader.chunkCount(region(new byte[10])));
        assertEquals(0, RegionReader.chunkCount(region(new byte[0])));
    }

    @Test
    void missingFilesAreReported() {
        assertThrows(NoSuchFileException.class, () -> RegionReader.chunkCount(root.resolve("missing.mca")));
    }
}
