package com.gabrielmartins.slimify.bukkit.command.sub;

import java.util.List;
import java.util.function.Supplier;

import org.bukkit.command.CommandSender;

import com.gabrielmartins.slimify.api.Slimify;
import com.gabrielmartins.slimify.bukkit.command.AbstractSubCommand;
import com.gabrielmartins.slimify.bukkit.command.FolderResolver;
import com.gabrielmartins.slimify.bukkit.message.Messages;

public final class ConvertCommand extends AbstractSubCommand {

    private final Supplier<Slimify> slimify;
    private final FolderResolver folders;

    public ConvertCommand(Supplier<Slimify> slimify, FolderResolver folders) {
        super("convert", "slimify.command.convert", "convert <folder> [name]", "Converts one world folder to a .slime file");
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
            Messages.result(sender, args.length > 1 ? current.convert(world, args[1]) : current.convert(world));
        }, () -> Messages.error(sender, "Invalid folder: " + args[0]));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return args.length == 1 ? folders.suggestions(args[0]) : List.of();
    }
}
