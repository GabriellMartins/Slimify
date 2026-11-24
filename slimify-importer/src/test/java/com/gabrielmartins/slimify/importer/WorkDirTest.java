package com.gabrielmartins.slimify.importer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class WorkDirTest {

    protected Path root;

    @BeforeEach
    void createRoot() throws IOException {
        root = Files.createTempDirectory("slimify-test");
    }

    @AfterEach
    void deleteRoot() throws IOException {
        System.gc();
        try (Stream<Path> files = Files.walk(root)) {
            files.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        }
    }
}
