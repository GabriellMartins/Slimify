package com.gabrielmartins.slimify.bukkit.event;

import org.bukkit.event.Event;

import com.gabrielmartins.slimify.api.result.ConversionResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class SlimifyEvent extends Event {

    private final ConversionResult result;
}
