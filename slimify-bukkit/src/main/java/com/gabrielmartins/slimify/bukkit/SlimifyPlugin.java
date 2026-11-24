package com.gabrielmartins.slimify.bukkit;

import java.nio.file.Path;
import java.util.List;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.gabrielmartins.slimify.api.Slimify;
import com.gabrielmartins.slimify.bukkit.command.FolderResolver;
import com.gabrielmartins.slimify.bukkit.command.SlimifyCommand;
import com.gabrielmartins.slimify.bukkit.command.SubCommand;
import com.gabrielmartins.slimify.bukkit.command.sub.ConvertAllCommand;
import com.gabrielmartins.slimify.bukkit.command.sub.ConvertCommand;
import com.gabrielmartins.slimify.bukkit.command.sub.DeleteCommand;
import com.gabrielmartins.slimify.bukkit.command.sub.InfoCommand;
import com.gabrielmartins.slimify.bukkit.command.sub.ListCommand;
import com.gabrielmartins.slimify.bukkit.command.sub.ReloadCommand;
import com.gabrielmartins.slimify.bukkit.config.SlimifyConfig;
import com.gabrielmartins.slimify.bukkit.listener.BukkitBridge;
import com.gabrielmartins.slimify.bukkit.service.ConsoleReporter;
import com.gabrielmartins.slimify.bukkit.service.StartupConverter;
import com.gabrielmartins.slimify.importer.SlimeImporter;

import lombok.Getter;

public final class SlimifyPlugin extends JavaPlugin {

    @Getter
    private volatile Slimify slimify;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        SlimifyConfig config = SlimifyConfig.from(getConfig());
        slimify = create(config);
        if (config.isConvertOnStartup()) {
            new StartupConverter(this::getSlimify, new ConsoleReporter(getLogger())).run();
        }
    }

    @Override
    public void onEnable() {
        register();
        PluginCommand command = getCommand("slimify");
        SlimifyCommand executor = new SlimifyCommand(subCommands());
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    @Override
    public void onDisable() {
        getServer().getServicesManager().unregisterAll(this);
    }

    public void reload() {
        reloadConfig();
        getServer().getServicesManager().unregisterAll(this);
        slimify = create(SlimifyConfig.from(getConfig()));
        register();
    }

    private Slimify create(SlimifyConfig config) {
        Path root = getServer().getWorldContainer().toPath().toAbsolutePath().normalize();
        Slimify created = SlimeImporter.create(config.toOptions(root));
        created.addListener(new BukkitBridge(getServer().getPluginManager()));
        return created;
    }

    private void register() {
        getServer().getServicesManager().register(Slimify.class, slimify, this, ServicePriority.Normal);
    }

    private List<SubCommand> subCommands() {
        FolderResolver folders = new FolderResolver(this::getSlimify);
        return List.of(
                new ConvertCommand(this::getSlimify, folders),
                new ConvertAllCommand(this::getSlimify),
                new ListCommand(this::getSlimify),
                new InfoCommand(this::getSlimify, folders),
                new DeleteCommand(this::getSlimify),
                new ReloadCommand(this::reload));
    }
}
