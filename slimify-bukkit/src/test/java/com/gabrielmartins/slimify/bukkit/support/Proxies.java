package com.gabrielmartins.slimify.bukkit.support;

import java.lang.reflect.Proxy;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Proxies {

    public CommandSender sender(List<String> messages, boolean permitted) {
        return (CommandSender) Proxy.newProxyInstance(Proxies.class.getClassLoader(), new Class<?>[] {CommandSender.class}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "sendMessage" -> {
                    if (args[0] instanceof String message) {
                        messages.add(message.replaceAll("§.", ""));
                    }
                    return null;
                }
                case "hasPermission" -> {
                    return permitted;
                }
                case "getName" -> {
                    return "tester";
                }
                default -> {
                    return null;
                }
            }
        });
    }

    public PluginManager pluginManager(List<Event> fired) {
        return (PluginManager) Proxy.newProxyInstance(Proxies.class.getClassLoader(), new Class<?>[] {PluginManager.class}, (proxy, method, args) -> {
            if (method.getName().equals("callEvent")) {
                fired.add((Event) args[0]);
            }
            return null;
        });
    }
}
