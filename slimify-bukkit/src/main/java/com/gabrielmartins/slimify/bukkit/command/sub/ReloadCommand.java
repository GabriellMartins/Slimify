package com.gabrielmartins.slimify.bukkit.command.sub;

import org.bukkit.command.CommandSender;

import com.gabrielmartins.slimify.bukkit.command.AbstractSubCommand;
import com.gabrielmartins.slimify.bukkit.message.Messages;

public final class ReloadCommand extends AbstractSubCommand {

    private final Runnable reload;

    public ReloadCommand(Runnable reload) {
        super("reload", "slimify.command.reload", "reload", "Reloads config.yml");
        this.reload = reload;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        reload.run();
        Messages.success(sender, "Configuration reloaded");
    }
}
