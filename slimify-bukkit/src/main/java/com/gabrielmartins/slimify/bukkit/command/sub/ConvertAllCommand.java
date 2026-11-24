package com.gabrielmartins.slimify.bukkit.command.sub;

import java.util.function.Supplier;

import org.bukkit.command.CommandSender;

import com.gabrielmartins.slimify.api.Slimify;
import com.gabrielmartins.slimify.bukkit.command.AbstractSubCommand;
import com.gabrielmartins.slimify.bukkit.message.Messages;

public final class ConvertAllCommand extends AbstractSubCommand {

    private final Supplier<Slimify> slimify;

    public ConvertAllCommand(Supplier<Slimify> slimify) {
        super("convertall", "slimify.command.convertall", "convertall", "Converts every world matching the configured names");
        this.slimify = slimify;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Messages.report(sender, slimify.get().convertAll());
    }
}
