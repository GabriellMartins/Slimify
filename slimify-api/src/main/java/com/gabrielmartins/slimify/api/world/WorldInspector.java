package com.gabrielmartins.slimify.api.world;

import java.nio.file.Path;
import java.util.List;

import com.gabrielmartins.slimify.api.exception.InvalidWorldException;
import com.gabrielmartins.slimify.api.option.WorldMatcher;

public interface WorldInspector {

    boolean isWorld(Path folder);

    WorldInfo inspect(Path folder) throws InvalidWorldException;

    List<Path> scan(Path root, WorldMatcher matcher);
}
