package com.gabrielmartins.slimify.api.option;

import java.util.Locale;
import java.util.Set;

@FunctionalInterface
public interface WorldMatcher {

    boolean matches(String folderName);

    static WorldMatcher any() {
        return name -> true;
    }

    static WorldMatcher containing(String text) {
        String needle = text.toLowerCase(Locale.ROOT);
        return name -> name.toLowerCase(Locale.ROOT).contains(needle);
    }

    static WorldMatcher exactly(String... names) {
        Set<String> accepted = Set.of(names);
        return accepted::contains;
    }

    default WorldMatcher or(WorldMatcher other) {
        return name -> matches(name) || other.matches(name);
    }

    default WorldMatcher negate() {
        return name -> !matches(name);
    }
}
