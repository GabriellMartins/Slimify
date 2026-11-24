package com.gabrielmartins.slimify.bukkit.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FormatsTest {

    @Test
    void formatsBytesWithTheRightUnit() {
        assertEquals("0 B", Formats.bytes(0));
        assertEquals("1023 B", Formats.bytes(1023));
        assertEquals("1.0 KB", Formats.bytes(1024));
        assertEquals("1.5 MB", Formats.bytes(1536 * 1024));
        assertEquals("2.0 GB", Formats.bytes(2L * 1024 * 1024 * 1024));
        assertEquals("2048.0 GB", Formats.bytes(2048L * 1024 * 1024 * 1024));
    }
}
