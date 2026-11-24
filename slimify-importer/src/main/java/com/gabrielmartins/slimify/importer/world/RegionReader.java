package com.gabrielmartins.slimify.importer.world;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegionReader {

    private final int SECTOR = 4096;
    private final int ENTRY = 4;

    public int chunkCount(Path region) throws IOException {
        byte[] header;
        try (InputStream in = Files.newInputStream(region)) {
            header = in.readNBytes(SECTOR);
        }
        int chunks = 0;
        for (int entry = 0; entry + ENTRY <= header.length; entry += ENTRY) {
            if (header[entry] != 0 || header[entry + 1] != 0 || header[entry + 2] != 0) {
                chunks++;
            }
        }
        return chunks;
    }
}
