package com.gabrielmartins.slimify.bukkit.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import com.gabrielmartins.slimify.api.option.ConflictPolicy;
import com.gabrielmartins.slimify.api.option.SlimifyOptions;

class SlimifyConfigTest {

    private static final Path ROOT = Path.of("server").toAbsolutePath().normalize();

    private static SlimifyConfig load(String yaml) throws InvalidConfigurationException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.loadFromString(yaml);
        return SlimifyConfig.from(configuration);
    }

    @Test
    void emptyConfigurationUsesTheStandardBehavior() throws InvalidConfigurationException {
        SlimifyConfig config = load("");
        SlimifyOptions options = config.toOptions(ROOT);

        assertTrue(config.isConvertOnStartup());
        assertTrue(options.getMatcher().matches("world_the_end"));
        assertFalse(options.getMatcher().matches("lobby"));
        assertEquals(ConflictPolicy.SKIP, options.getConflictPolicy());
        assertEquals(ROOT.resolve("slime_worlds"), options.outputDirectory());
        assertEquals(0, options.getLimits().maxChunks());
    }

    @Test
    void readsEveryOption() throws InvalidConfigurationException {
        SlimifyConfig config = load("""
                convert-on-startup: false
                output-folder: converted/slime
                match: [world, arena]
                on-conflict: overwrite
                limits:
                  max-chunks: 500
                  max-size-mb: 2
                """);
        SlimifyOptions options = config.toOptions(ROOT);

        assertFalse(config.isConvertOnStartup());
        assertTrue(options.getMatcher().matches("Arena_3"));
        assertTrue(options.getMatcher().matches("world"));
        assertFalse(options.getMatcher().matches("lobby"));
        assertEquals(ConflictPolicy.OVERWRITE, options.getConflictPolicy());
        assertEquals(ROOT.resolve("converted").resolve("slime"), options.outputDirectory());
        assertEquals(500, options.getLimits().maxChunks());
        assertEquals(2L * 1024 * 1024, options.getLimits().maxBytes());
    }

    @Test
    void unknownConflictPolicyFallsBackToSkip() throws InvalidConfigurationException {
        assertEquals(ConflictPolicy.SKIP, load("on-conflict: explode").getConflictPolicy());
    }

    @Test
    void blankMatchEntriesAreIgnored() throws InvalidConfigurationException {
        SlimifyConfig config = load("match: ['', '  ']");

        assertEquals(java.util.List.of("world"), config.getMatch());
    }

    @Test
    void outputCannotLeaveTheServerFolder() throws InvalidConfigurationException {
        assertEquals(ROOT.resolve("slime_worlds"), load("output-folder: ../../elsewhere").toOptions(ROOT).outputDirectory());
        assertEquals(ROOT.resolve("slime_worlds"), load("output-folder: .").toOptions(ROOT).outputDirectory());
    }
}
