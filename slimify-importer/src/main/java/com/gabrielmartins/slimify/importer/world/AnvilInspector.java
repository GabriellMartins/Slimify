package com.gabrielmartins.slimify.importer.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.gabrielmartins.slimify.api.exception.InvalidWorldException;
import com.gabrielmartins.slimify.api.exception.StorageException;
import com.gabrielmartins.slimify.api.option.WorldMatcher;
import com.gabrielmartins.slimify.api.world.WorldInfo;
import com.gabrielmartins.slimify.api.world.WorldInspector;

public final class AnvilInspector implements WorldInspector {

    private static final String LEVEL_FILE = "level.dat";
    private static final String REGION_FOLDER = "region";
    private static final String REGION_EXTENSION = ".mca";

    @Override
    public boolean isWorld(Path folder) {
        return Files.isDirectory(folder)
                && Files.isRegularFile(folder.resolve(LEVEL_FILE))
                && Files.isDirectory(folder.resolve(REGION_FOLDER));
    }

    @Override
    public WorldInfo inspect(Path folder) {
        if (!isWorld(folder)) {
            throw new InvalidWorldException(folder, "Not a world folder (level.dat or region is missing)");
        }
        try (Stream<Path> files = Files.list(folder.resolve(REGION_FOLDER))) {
            List<Path> regions = files.filter(file -> file.getFileName().toString().endsWith(REGION_EXTENSION)).toList();
            long chunks = 0;
            long size = 0;
            for (Path region : regions) {
                chunks += RegionReader.chunkCount(region);
                size += Files.size(region);
            }
            return new WorldInfo(folder.getFileName().toString(), folder, regions.size(), chunks, size);
        } catch (IOException e) {
            throw new InvalidWorldException(folder, "Cannot read the region folder: " + e.getMessage());
        }
    }

    @Override
    public List<Path> scan(Path root, WorldMatcher matcher) {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> children = Files.list(root)) {
            return children
                    .filter(this::isWorld)
                    .filter(folder -> matcher.matches(folder.getFileName().toString()))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new StorageException("Cannot list " + root, e);
        }
    }
}
