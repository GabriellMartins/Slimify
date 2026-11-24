package com.gabrielmartins.slimify.bukkit.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Event;
import org.junit.jupiter.api.Test;

import com.gabrielmartins.slimify.api.result.ConversionResult;
import com.gabrielmartins.slimify.bukkit.event.SlimifyEvent;
import com.gabrielmartins.slimify.bukkit.event.WorldConversionFailedEvent;
import com.gabrielmartins.slimify.bukkit.event.WorldConvertedEvent;
import com.gabrielmartins.slimify.bukkit.support.Proxies;

class BukkitBridgeTest {

    private static final Path PATH = Path.of("world");

    private final List<Event> fired = new ArrayList<>();
    private final BukkitBridge bridge = new BukkitBridge(Proxies.pluginManager(fired));

    @Test
    void successFiresTheConvertedEvent() {
        ConversionResult result = ConversionResult.converted("world", PATH, PATH, 3);

        bridge.finished(result);

        assertEquals(1, fired.size());
        assertInstanceOf(WorldConvertedEvent.class, fired.get(0));
        assertEquals(result, ((SlimifyEvent) fired.get(0)).getResult());
    }

    @Test
    void failureFiresTheFailedEvent() {
        bridge.finished(ConversionResult.failed("world", PATH, PATH, 3, "boom"));

        assertEquals(1, fired.size());
        assertInstanceOf(WorldConversionFailedEvent.class, fired.get(0));
    }

    @Test
    void skippedFiresNothing() {
        bridge.finished(ConversionResult.skipped("world", PATH, PATH, "exists"));

        assertTrue(fired.isEmpty());
    }

    @Test
    void eventsHaveTheirOwnHandlerLists() {
        assertEquals(WorldConvertedEvent.getHandlerList(), new WorldConvertedEvent(ConversionResult.converted("w", PATH, PATH, 1)).getHandlers());
        assertTrue(WorldConvertedEvent.getHandlerList() != WorldConversionFailedEvent.getHandlerList());
    }
}
