package com.gabrielmartins.slimify.importer.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.gabrielmartins.slimify.api.exception.StorageException;
import com.gabrielmartins.slimify.api.format.SlimeFormat;
import com.gabrielmartins.slimify.api.storage.SlimeFile;
import com.gabrielmartins.slimify.api.storage.SlimeStorage;
import com.gabrielmartins.slimify.importer.naming.SlimeNames;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public final class FileSlimeStorage implements SlimeStorage {

    @Getter
    @Accessors(fluent = true)
    private final Path directory;

    @Override
    public List<SlimeFile> list() {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(SlimeFormat.EXTENSION))
                    .map(this::describe)
                    .sorted(Comparator.comparing(SlimeFile::name))
                    .toList();
        } catch (IOException e) {
            throw new StorageException("Cannot list " + directory, e);
        }
    }

    @Override
    public boolean exists(String name) {
        return Files.isRegularFile(locate(name));
    }

    @Override
    public Path locate(String name) {
        return directory.resolve(SlimeNames.require(name) + SlimeFormat.EXTENSION);
    }

    @Override
    public boolean delete(String name) {
        try {
            return Files.deleteIfExists(locate(name));
        } catch (IOException e) {
            throw new StorageException("Cannot delete " + name, e);
        }
    }

    private SlimeFile describe(Path file) {
        String fileName = file.getFileName().toString();
        try {
            return new SlimeFile(fileName.substring(0, fileName.length() - SlimeFormat.EXTENSION.length()), file, Files.size(file),
                    Files.getLastModifiedTime(file).toInstant());
        } catch (IOException e) {
            throw new StorageException("Cannot read " + file, e);
        }
    }
}
