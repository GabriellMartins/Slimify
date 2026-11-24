package com.gabrielmartins.slimify.bukkit.event;

import org.bukkit.event.HandlerList;

import com.gabrielmartins.slimify.api.result.ConversionResult;

public final class WorldConversionFailedEvent extends SlimifyEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public WorldConversionFailedEvent(ConversionResult result) {
        super(result);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
