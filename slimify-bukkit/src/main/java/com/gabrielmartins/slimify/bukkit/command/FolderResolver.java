package com.gabrielmartins.slimify.bukkit.command;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import com.gabrielmartins.slimify.api.Slimify;
import com.gabrielmartins.slimify.api.option.WorldMatcher;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FolderResolver {

    private final Supplier<Slimify> slimify;

    public Optional<Path> resolve(String folder) {
        Path root = slimify.get().options().getRoot().toAbsolutePath().normalize();
        Path resolved = root.resolve(folder).normalize();
        boolean inside = resolved.startsWith(root) && !resolved.equals(root);
        return inside ? Optional.of(resolved) : Optional.empty();
    }

    public List<String> suggestions(String prefix) {
        Slimify current = slimify.get();
        String lower = prefix.toLowerCase(Locale.ROOT);
        return current.inspector().scan(current.options().getRoot(), WorldMatcher.any()).stream()
                .map(folder -> folder.getFileName().toString())
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(lower))
                .toList();
    }
}
