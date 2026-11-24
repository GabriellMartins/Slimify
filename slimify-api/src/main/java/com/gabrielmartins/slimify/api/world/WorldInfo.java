package com.gabrielmartins.slimify.api.world;

import java.nio.file.Path;

public record WorldInfo(String name, Path path, int regionFiles, long chunks, long sizeBytes) {

    public boolean empty() {
        return chunks == 0;
    }
}
