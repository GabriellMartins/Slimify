package com.gabrielmartins.slimify.bukkit.command.sub;

import java.util.List;
import java.util.function.Supplier;

import org.bukkit.command.CommandSender;

import com.gabrielmartins.slimify.api.Slimify;
import com.gabrielmartins.slimify.api.world.WorldInfo;
import com.gabrielmartins.slimify.bukkit.command.AbstractSubCommand;
import com.gabrielmartins.slimify.bukkit.command.FolderResolver;
import com.gabrielmartins.slimify.bukkit.message.Formats;
import com.gabrielmartins.slimify.bukkit.message.Messages;

public final class InfoCommand extends AbstractSubCommand {

    private final Supplier<Slimify> slimify;
    private final FolderResolver folders;

    public InfoCommand(Supplier<Slimify> slimify, FolderResolver folders) {
        super("info", "slimify.command.info", "info <folder>", "Shows chunks and size of a world folder");
        this.slimify = slimify;
        this.folders = folders;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Messages.error(sender, "Usage: /slimify " + usage());
            return;
        }
        folders.resolve(args[0]).ifPresentOrElse(world -> {
            Slimify current = slimify.get();
            WorldInfo info = current.inspector().inspect(world);
            Messages.info(sender, info.name() + ": " + info.chunks() + " chunks in " + info.regionFiles() + " region file(s), " + Formats.bytes(info.sizeBytes()));
            String slimeName = current.options().getNamer().name(info.name());
            boolean converted = current.storage().exists(slimeName);
            Messages.info(sender, converted ? "Already converted as " + slimeName : "Not converted yet");
        }, () -> Messages.error(sender, "Invalid folder: " + args[0]));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return args.length == 1 ? folders.suggestions(args[0]) : List.of();
    }
}
