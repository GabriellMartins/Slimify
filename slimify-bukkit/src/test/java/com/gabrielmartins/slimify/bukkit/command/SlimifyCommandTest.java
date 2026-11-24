package com.gabrielmartins.slimify.bukkit.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gabrielmartins.slimify.api.Slimify;
import com.gabrielmartins.slimify.api.exception.ConversionFailedException;
import com.gabrielmartins.slimify.api.format.SlimeFormat;
import com.gabrielmartins.slimify.api.option.SlimifyOptions;
import com.gabrielmartins.slimify.bukkit.command.sub.ConvertAllCommand;
import com.gabrielmartins.slimify.bukkit.command.sub.ConvertCommand;
import com.gabrielmartins.slimify.bukkit.command.sub.DeleteCommand;
import com.gabrielmartins.slimify.bukkit.command.sub.InfoCommand;
import com.gabrielmartins.slimify.bukkit.command.sub.ListCommand;
import com.gabrielmartins.slimify.bukkit.command.sub.ReloadCommand;
import com.gabrielmartins.slimify.bukkit.support.Proxies;
import com.gabrielmartins.slimify.importer.SlimeImporter;

class SlimifyCommandTest {

    private final List<String> messages = new ArrayList<>();
    private final AtomicInteger reloads = new AtomicInteger();
    private Path root;
    private Slimify slimify;
    private SlimifyCommand command;

    @BeforeEach
    void setUp() throws IOException {
        root = Files.createTempDirectory("slimify-command");
        slimify = SlimeImporter.create(SlimifyOptions.builder().root(root).build(), (world, target) -> {
            try {
                Files.createDirectories(target.getParent());
                Files.write(target, new byte[] {SlimeFormat.MAGIC_FIRST, SlimeFormat.MAGIC_SECOND, 9, 1});
            } catch (IOException e) {
                throw new ConversionFailedException("world", e.getMessage());
            }
        });
        FolderResolver folders = new FolderResolver(() -> slimify);
        command = new SlimifyCommand(List.of(
                new ConvertCommand(() -> slimify, folders),
                new ConvertAllCommand(() -> slimify),
                new ListCommand(() -> slimify),
                new InfoCommand(() -> slimify, folders),
                new DeleteCommand(() -> slimify),
                new ReloadCommand(reloads::incrementAndGet)));
        makeWorld("world");
    }

    @AfterEach
    void tearDown() throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            files.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        }
    }

    private void makeWorld(String name) throws IOException {
        Path folder = Files.createDirectories(root.resolve(name).resolve("region"));
        Files.write(root.resolve(name).resolve("level.dat"), new byte[1]);
        byte[] header = new byte[8192];
        header[2] = 2;
        Files.write(folder.resolve("r.0.0.mca"), header);
    }

    private void run(CommandSender sender, String... args) {
        command.onCommand(sender, null, "slimify", args);
    }

    private CommandSender sender(boolean permitted) {
        return Proxies.sender(messages, permitted);
    }

    @Test
    void noArgumentsShowTheHelpForPermittedCommands() {
        run(sender(true));

        assertTrue(messages.stream().anyMatch(line -> line.contains("/slimify convert <folder> [name]")));
        assertTrue(messages.stream().anyMatch(line -> line.contains("/slimify reload")));
    }

    @Test
    void helpHidesCommandsTheSenderCannotUse() {
        run(sender(false));

        assertEquals(List.of("[Slimify] Commands:"), messages);
    }

    @Test
    void commandsRequirePermission() {
        run(sender(false), "convertall");

        assertEquals(List.of("[Slimify] You do not have permission to do that"), messages);
        assertTrue(slimify.storage().list().isEmpty());
    }

    @Test
    void convertWritesTheSlimeFile() {
        run(sender(true), "convert", "world");

        assertTrue(messages.get(0).contains("world converted to world.slime"));
        assertTrue(slimify.storage().exists("world"));
    }

    @Test
    void convertAcceptsAnExplicitName() {
        run(sender(true), "convert", "world", "lobby_copy");

        assertTrue(slimify.storage().exists("lobby_copy"));
    }

    @Test
    void convertRejectsFoldersOutsideTheServer() {
        run(sender(true), "convert", "../outside");

        assertTrue(messages.get(0).contains("Invalid folder"));
    }

    @Test
    void convertWithoutAFolderShowsTheUsage() {
        run(sender(true), "convert");

        assertTrue(messages.get(0).contains("Usage: /slimify convert <folder> [name]"));
    }

    @Test
    void convertAllSummarizes() throws IOException {
        makeWorld("world_nether");

        run(sender(true), "convertall");

        assertTrue(messages.get(messages.size() - 1).contains("2 converted, 0 skipped, 0 failed"));
    }

    @Test
    void listShowsConvertedWorlds() {
        run(sender(true), "list");
        run(sender(true), "convert", "world");
        run(sender(true), "list");

        assertEquals("[Slimify] No slime worlds yet", messages.get(0));
        assertTrue(messages.stream().anyMatch(line -> line.contains("- world (4 B)")));
    }

    @Test
    void infoShowsChunksAndConversionState() {
        run(sender(true), "info", "world");

        assertTrue(messages.get(0).contains("world: 1 chunks in 1 region file(s)"));
        assertEquals("[Slimify] Not converted yet", messages.get(1));
    }

    @Test
    void deleteRemovesOnlyTheSlimeFile() {
        run(sender(true), "convert", "world");
        run(sender(true), "delete", "world");
        run(sender(true), "delete", "world");

        assertFalse(slimify.storage().exists("world"));
        assertTrue(Files.isDirectory(root.resolve("world")));
        assertTrue(messages.get(1).contains("world.slime deleted"));
        assertTrue(messages.get(2).contains("No slime world named world"));
    }

    @Test
    void invalidNamesAreReportedInsteadOfThrown() {
        run(sender(true), "delete", "../x");

        assertTrue(messages.get(0).contains("Invalid slime name"));
    }

    @Test
    void reloadRunsTheCallback() {
        run(sender(true), "reload");

        assertEquals(1, reloads.get());
        assertEquals("[Slimify] Configuration reloaded", messages.get(0));
    }

    @Test
    void tabCompletionSuggestsCommandsAndWorlds() throws IOException {
        makeWorld("lobby");
        CommandSender sender = sender(true);

        assertEquals(List.of("convert", "convertall"), command.onTabComplete(sender, null, "slimify", new String[] {"conv"}));
        assertEquals(List.of("lobby"), command.onTabComplete(sender, null, "slimify", new String[] {"convert", "lo"}));
        assertEquals(List.of(), command.onTabComplete(sender, null, "slimify", new String[] {"nope", "x"}));
        assertEquals(List.of(), command.onTabComplete(sender(false), null, "slimify", new String[] {"c"}));
    }
}
