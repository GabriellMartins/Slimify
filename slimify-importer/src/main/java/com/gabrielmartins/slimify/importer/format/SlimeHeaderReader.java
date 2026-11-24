package com.gabrielmartins.slimify.importer.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.gabrielmartins.slimify.api.format.SlimeFormat;
import com.gabrielmartins.slimify.api.format.SlimeHeader;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SlimeHeaderReader {

    public Optional<SlimeHeader> read(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] head = in.readNBytes(SlimeFormat.HEADER_LENGTH);
            boolean valid = head.length == SlimeFormat.HEADER_LENGTH
                    && head[0] == SlimeFormat.MAGIC_FIRST
                    && head[1] == SlimeFormat.MAGIC_SECOND;
            return valid ? Optional.of(new SlimeHeader(head[2])) : Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
