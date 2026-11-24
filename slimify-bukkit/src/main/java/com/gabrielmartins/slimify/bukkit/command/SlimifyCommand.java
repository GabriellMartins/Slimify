package com.gabrielmartins.slimify.bukkit.command;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.gabrielmartins.slimify.api.exception.SlimifyException;
import com.gabrielmartins.slimify.bukkit.message.Messages;

public final class SlimifyCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> commands = new LinkedHashMap<>();

    public SlimifyCommand(List<SubCommand> subCommands) {
        subCommands.forEach(command -> commands.put(command.name(), command));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SubCommand sub = args.length == 0 ? null : commands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            help(sender, label);
            return true;
        }
        if (!sender.hasPermission(sub.permission())) {
            Messages.error(sender, "You do not have permission to do that");
            return true;
        }
        try {
            sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        } catch (SlimifyException e) {
            Messages.error(sender, e.getMessage());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return commands.values().stream()
                    .filter(sub -> sender.hasPermission(sub.permission()))
                    .map(SubCommand::name)
                    .filter(name -> name.startsWith(prefix))
                    .toList();
        }
        SubCommand sub = commands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null || !sender.hasPermission(sub.permission())) {
            return List.of();
        }
        return sub.complete(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    private void help(CommandSender sender, String label) {
        Messages.info(sender, "Commands:");
        commands.values().stream()
                .filter(sub -> sender.hasPermission(sub.permission()))
                .forEach(sub -> Messages.info(sender, "/" + label + " " + sub.usage() + " - " + sub.description()));
    }
}
