package com.gabrielmartins.slimify.api.option;

import java.util.Locale;

@FunctionalInterface
public interface WorldNamer {

    String name(String folderName);

    static WorldNamer same() {
        return folderName -> folderName;
    }

    static WorldNamer lowerCase() {
        return folderName -> folderName.toLowerCase(Locale.ROOT);
    }

    static WorldNamer prefixed(String prefix) {
        return folderName -> prefix + folderName;
    }

    static WorldNamer suffixed(String suffix) {
        return folderName -> folderName + suffix;
    }
}
