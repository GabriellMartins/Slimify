package com.gabrielmartins.slimify.api.option;

import java.util.Optional;

import com.gabrielmartins.slimify.api.world.WorldInfo;

public record WorldLimits(long maxChunks, long maxBytes) {

    private static final WorldLimits UNLIMITED = new WorldLimits(0, 0);

    public static WorldLimits unlimited() {
        return UNLIMITED;
    }

    public static WorldLimits of(long maxChunks, long maxMegabytes) {
        return new WorldLimits(Math.max(0, maxChunks), Math.max(0, maxMegabytes) * 1024 * 1024);
    }

    public Optional<String> violation(WorldInfo info) {
        if (maxChunks > 0 && info.chunks() > maxChunks) {
            return Optional.of("World has " + info.chunks() + " chunks, above the limit of " + maxChunks);
        }
        if (maxBytes > 0 && info.sizeBytes() > maxBytes) {
            return Optional.of("World uses " + info.sizeBytes() / 1024 / 1024 + " MB, above the limit of " + maxBytes / 1024 / 1024 + " MB");
        }
        return Optional.empty();
    }
}
