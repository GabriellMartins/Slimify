package com.gabrielmartins.slimify.importer.naming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.gabrielmartins.slimify.api.exception.InvalidSlimeNameException;

class SlimeNamesTest {

    @ParameterizedTest
    @ValueSource(strings = {"world", "World_Nether", "arena-1", "lobby.v2", "1st", "a"})
    void acceptsSafeNames(String name) {
        assertTrue(SlimeNames.isValid(name));
        assertEquals(name, SlimeNames.require(name));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"../x", "a/b", "a\\b", ".hidden", "-dash", "with space", "café", "name.slime/", "a:b"})
    void rejectsUnsafeNames(String name) {
        assertFalse(SlimeNames.isValid(name));
        assertThrows(InvalidSlimeNameException.class, () -> SlimeNames.require(name));
    }

    @Test
    void rejectsNamesThatAreTooLong() {
        assertTrue(SlimeNames.isValid("a".repeat(64)));
        assertFalse(SlimeNames.isValid("a".repeat(65)));
    }

    @Test
    void exceptionCarriesTheName() {
        InvalidSlimeNameException error = assertThrows(InvalidSlimeNameException.class, () -> SlimeNames.require("../x"));

        assertEquals("../x", error.getName());
    }
}
