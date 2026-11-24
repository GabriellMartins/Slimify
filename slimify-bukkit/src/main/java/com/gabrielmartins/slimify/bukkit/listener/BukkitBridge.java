package com.gabrielmartins.slimify.bukkit.listener;

import org.bukkit.plugin.PluginManager;

import com.gabrielmartins.slimify.api.event.ConversionListener;
import com.gabrielmartins.slimify.api.result.ConversionResult;
import com.gabrielmartins.slimify.bukkit.event.WorldConversionFailedEvent;
import com.gabrielmartins.slimify.bukkit.event.WorldConvertedEvent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class BukkitBridge implements ConversionListener {

    private final PluginManager plugins;

    @Override
    public void finished(ConversionResult result) {
        switch (result.status()) {
            case CONVERTED -> plugins.callEvent(new WorldConvertedEvent(result));
            case FAILED -> plugins.callEvent(new WorldConversionFailedEvent(result));
            case SKIPPED -> {
            }
        }
    }
}
