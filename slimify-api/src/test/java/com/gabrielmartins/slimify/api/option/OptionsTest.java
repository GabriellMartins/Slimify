package com.gabrielmartins.slimify.api.option;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.gabrielmartins.slimify.api.world.WorldInfo;

class OptionsTest {

    private static WorldInfo world(long chunks, long bytes) {
        return new WorldInfo("world", Path.of("world"), 1, chunks, bytes);
    }

    @Test
    void defaultsMatchTheStandardBehavior() {
        SlimifyOptions options = SlimifyOptions.builder().root(Path.of("server")).build();

        assertTrue(options.getMatcher().matches("world_nether"));
        assertFalse(options.getMatcher().matches("lobby"));
        assertEquals("world", options.getNamer().name("world"));
        assertEquals(ConflictPolicy.SKIP, options.getConflictPolicy());
        assertEquals(WorldLimits.unlimited(), options.getLimits());
        assertEquals(Path.of("server").resolve("slime_worlds"), options.outputDirectory());
    }

    @Test
    void customOutputWins() {
        SlimifyOptions options = SlimifyOptions.builder().root(Path.of("server")).output(Path.of("custom")).build();

        assertEquals(Path.of("custom"), options.outputDirectory());
    }

    @Test
    void rootIsRequired() {
        assertThrows(NullPointerException.class, () -> SlimifyOptions.builder().build());
    }

    @Test
    void toBuilderKeepsEverythingElse() {
        SlimifyOptions base = SlimifyOptions.builder().root(Path.of("server")).conflictPolicy(ConflictPolicy.FAIL).build();

        SlimifyOptions copy = base.toBuilder().output(Path.of("out")).build();

        assertEquals(ConflictPolicy.FAIL, copy.getConflictPolicy());
        assertEquals(Path.of("out"), copy.outputDirectory());
    }

    @Test
    void matchersCombine() {
        WorldMatcher matcher = WorldMatcher.exactly("lobby").or(WorldMatcher.containing("arena"));

        assertTrue(matcher.matches("lobby"));
        assertTrue(matcher.matches("Arena_1"));
        assertFalse(matcher.matches("world"));
        assertTrue(matcher.negate().matches("world"));
        assertTrue(WorldMatcher.any().matches("anything"));
    }

    @Test
    void namersTransformTheFolderName() {
        assertEquals("World_Nether", WorldNamer.same().name("World_Nether"));
        assertEquals("world_nether", WorldNamer.lowerCase().name("World_Nether"));
        assertEquals("lh_world", WorldNamer.prefixed("lh_").name("world"));
        assertEquals("world_v2", WorldNamer.suffixed("_v2").name("world"));
    }

    @Test
    void unlimitedNeverComplains() {
        assertTrue(WorldLimits.unlimited().violation(world(1_000_000, Long.MAX_VALUE)).isEmpty());
    }

    @Test
    void limitsReportTheFirstExceededRule() {
        WorldLimits limits = WorldLimits.of(100, 1);

        assertTrue(limits.violation(world(100, 1024)).isEmpty());
        assertTrue(limits.violation(world(101, 1024)).orElseThrow().contains("101 chunks"));
        assertTrue(limits.violation(world(10, 2L * 1024 * 1024)).orElseThrow().contains("MB"));
    }

    @Test
    void negativeLimitsAreTreatedAsUnlimited() {
        assertEquals(WorldLimits.unlimited(), WorldLimits.of(-5, -5));
    }
}
