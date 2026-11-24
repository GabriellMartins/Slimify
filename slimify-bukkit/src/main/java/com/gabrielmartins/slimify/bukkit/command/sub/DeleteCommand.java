package com.gabrielmartins.slimify.bukkit.command.sub;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import org.bukkit.command.CommandSender;

import com.gabrielmartins.slimify.api.Slimify;
import com.gabrielmartins.slimify.api.storage.SlimeFile;
import com.gabrielmartins.slimify.bukkit.command.AbstractSubCommand;
import com.gabrielmartins.slimify.bukkit.message.Messages;

public final class DeleteCommand extends AbstractSubCommand {

    private final Supplier<Slimify> slimify;

    public DeleteCommand(Supplier<Slimify> slimify) {
        super("delete", "slimify.command.delete", "delete <name>", "Deletes a .slime file (the world folder is untouched)");
        this.slimify = slimify;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            Messages.error(sender, "Usage: /slimify " + usage());
            return;
        }
        boolean deleted = slimify.get().storage().delete(args[0]);
        if (deleted) {
            Messages.success(sender, args[0] + ".slime deleted");
        } else {
            Messages.error(sender, "No slime world named " + args[0]);
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return slimify.get().storage().list().stream()
                .map(SlimeFile::name)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
    }
}
