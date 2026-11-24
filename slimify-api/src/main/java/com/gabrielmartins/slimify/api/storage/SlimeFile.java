package com.gabrielmartins.slimify.api.storage;

import java.nio.file.Path;
import java.time.Instant;

public record SlimeFile(String name, Path path, long sizeBytes, Instant modified) {
}
