package com.gabrielmartins.slimify.bukkit.command;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface SubCommand {

    String name();

    String permission();

    String usage();

    String description();

    void execute(CommandSender sender, String[] args);

    default List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
