package com.gabrielmartins.slimify.bukkit.message;

import org.bukkit.command.CommandSender;

import com.gabrielmartins.slimify.api.result.ConversionReport;
import com.gabrielmartins.slimify.api.result.ConversionResult;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Messages {

    private final String PREFIX = "§8[§aSlimify§8] ";

    public void info(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + "§7" + message);
    }

    public void success(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + "§a" + message);
    }

    public void error(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + "§c" + message);
    }

    public void result(CommandSender sender, ConversionResult result) {
        switch (result.status()) {
            case CONVERTED -> success(sender, result.world() + " converted to " + result.target().getFileName() + " (" + result.elapsedMillis() + " ms)");
            case SKIPPED -> info(sender, result.world() + " skipped: " + result.detail());
            case FAILED -> error(sender, result.world() + " failed: " + result.detail());
        }
    }

    public void report(CommandSender sender, ConversionReport report) {
        if (report.empty()) {
            info(sender, "No world folders to convert");
            return;
        }
        report.results().forEach(result -> result(sender, result));
        if (report.allSucceeded()) {
            success(sender, report.summary());
        } else {
            error(sender, report.summary());
        }
    }
}
