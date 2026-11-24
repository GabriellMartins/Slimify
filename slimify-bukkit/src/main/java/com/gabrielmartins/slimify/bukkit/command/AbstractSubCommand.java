package com.gabrielmartins.slimify.bukkit.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public abstract class AbstractSubCommand implements SubCommand {

    private final String name;
    private final String permission;
    private final String usage;
    private final String description;
}
