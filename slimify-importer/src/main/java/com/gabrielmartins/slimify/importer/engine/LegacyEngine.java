package com.gabrielmartins.slimify.importer.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.gabrielmartins.slimify.api.exception.ConversionFailedException;
import com.gabrielmartins.slimify.api.format.SlimeFormat;
import com.grinderwolf.swm.importer.SWMImporter;

public final class LegacyEngine implements ConversionEngine {

    @Override
    public void convert(Path world, Path target) {
        String name = world.getFileName().toString();
        Path produced = Path.of(name + SlimeFormat.EXTENSION).toAbsolutePath();
        if (Files.exists(produced)) {
            throw new ConversionFailedException(name, "Remove the leftover file " + produced + " and try again");
        }
        String output = ConsoleCapture.run(() -> SWMImporter.main(new String[] {world.toAbsolutePath().toString()}));
        if (!Files.isRegularFile(produced)) {
            throw new ConversionFailedException(name, OutputParser.reason(output));
        }
        try {
            Files.createDirectories(target.toAbsolutePath().getParent());
            Files.move(produced, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ConversionFailedException(name, "Cannot write " + target.getFileName() + ": " + e.getMessage(), e);
        }
    }
}
