package com.gabrielmartins.slimify.api.option;

import java.nio.file.Path;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class SlimifyOptions {

    public static final String DEFAULT_OUTPUT = "slime_worlds";

    @NonNull
    Path root;

    Path output;

    @NonNull
    @Builder.Default
    WorldMatcher matcher = WorldMatcher.containing("world");

    @NonNull
    @Builder.Default
    WorldNamer namer = WorldNamer.same();

    @NonNull
    @Builder.Default
    ConflictPolicy conflictPolicy = ConflictPolicy.SKIP;

    @NonNull
    @Builder.Default
    WorldLimits limits = WorldLimits.unlimited();

    public Path outputDirectory() {
        return output != null ? output : root.resolve(DEFAULT_OUTPUT);
    }
}
