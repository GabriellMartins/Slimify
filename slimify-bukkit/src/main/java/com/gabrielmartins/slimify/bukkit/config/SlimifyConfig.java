package com.gabrielmartins.slimify.bukkit.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.bukkit.configuration.ConfigurationSection;

import com.gabrielmartins.slimify.api.option.ConflictPolicy;
import com.gabrielmartins.slimify.api.option.SlimifyOptions;
import com.gabrielmartins.slimify.api.option.WorldLimits;
import com.gabrielmartins.slimify.api.option.WorldMatcher;

import lombok.Value;

@Value
public class SlimifyConfig {

    private static final String DEFAULT_MATCH = "world";

    boolean convertOnStartup;
    String outputFolder;
    List<String> match;
    ConflictPolicy conflictPolicy;
    long maxChunks;
    long maxSizeMegabytes;

    public static SlimifyConfig from(ConfigurationSection section) {
        List<String> match = section.getStringList("match").stream().map(String::strip).filter(value -> !value.isEmpty()).toList();
        return new SlimifyConfig(
                section.getBoolean("convert-on-startup", true),
                section.getString("output-folder", SlimifyOptions.DEFAULT_OUTPUT),
                match.isEmpty() ? List.of(DEFAULT_MATCH) : match,
                policy(section.getString("on-conflict", "SKIP")),
                section.getLong("limits.max-chunks", 0),
                section.getLong("limits.max-size-mb", 0));
    }

    public SlimifyOptions toOptions(Path root) {
        WorldMatcher matcher = match.stream().map(WorldMatcher::containing).reduce(WorldMatcher::or).orElseGet(() -> WorldMatcher.containing(DEFAULT_MATCH));
        return SlimifyOptions.builder()
                .root(root)
                .output(output(root))
                .matcher(matcher)
                .conflictPolicy(conflictPolicy)
                .limits(WorldLimits.of(maxChunks, maxSizeMegabytes))
                .build();
    }

    private Path output(Path root) {
        Path folder = root.resolve(outputFolder).normalize();
        boolean inside = folder.startsWith(root) && !folder.equals(root);
        return inside ? folder : root.resolve(SlimifyOptions.DEFAULT_OUTPUT);
    }

    private static ConflictPolicy policy(String value) {
        try {
            return ConflictPolicy.valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ConflictPolicy.SKIP;
        }
    }
}
