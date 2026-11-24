package com.gabrielmartins.slimify.bukkit.command.sub;

import java.util.List;
import java.util.function.Supplier;

import org.bukkit.command.CommandSender;

import com.gabrielmartins.slimify.api.Slimify;
import com.gabrielmartins.slimify.api.storage.SlimeFile;
import com.gabrielmartins.slimify.bukkit.command.AbstractSubCommand;
import com.gabrielmartins.slimify.bukkit.message.Formats;
import com.gabrielmartins.slimify.bukkit.message.Messages;

public final class ListCommand extends AbstractSubCommand {

    private final Supplier<Slimify> slimify;

    public ListCommand(Supplier<Slimify> slimify) {
        super("list", "slimify.command.list", "list", "Lists the converted .slime files");
        this.slimify = slimify;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<SlimeFile> files = slimify.get().storage().list();
        if (files.isEmpty()) {
            Messages.info(sender, "No slime worlds yet");
            return;
        }
        Messages.info(sender, files.size() + " slime world(s):");
        files.forEach(file -> Messages.info(sender, "- " + file.name() + " (" + Formats.bytes(file.sizeBytes()) + ")"));
    }
}
