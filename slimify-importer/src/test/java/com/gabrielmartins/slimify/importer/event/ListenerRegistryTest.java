package com.gabrielmartins.slimify.importer.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gabrielmartins.slimify.api.event.ConversionListener;
import com.gabrielmartins.slimify.api.result.ConversionResult;

class ListenerRegistryTest {

    private static final Path PATH = Path.of("world");

    private final ListenerRegistry registry = new ListenerRegistry();
    private final List<String> heard = new ArrayList<>();

    private ConversionListener recorder(String id) {
        return new ConversionListener() {
            @Override
            public void started(String world, Path source, Path target) {
                heard.add(id + ":started:" + world);
            }

            @Override
            public void finished(ConversionResult result) {
                heard.add(id + ":finished:" + result.world());
            }
        };
    }

    @Test
    void listenersAreCalledInRegistrationOrder() {
        registry.add(recorder("a"));
        registry.add(recorder("b"));

        registry.started("world", PATH, PATH);
        registry.finished(ConversionResult.converted("world", PATH, PATH, 1));

        assertEquals(List.of("a:started:world", "b:started:world", "a:finished:world", "b:finished:world"), heard);
    }

    @Test
    void removedListenersStopHearing() {
        ConversionListener listener = recorder("a");
        registry.add(listener);
        registry.remove(listener);

        registry.started("world", PATH, PATH);

        assertEquals(List.of(), heard);
    }

    @Test
    void aFailingListenerDoesNotBlockTheOthers() {
        registry.add(new ConversionListener() {
            @Override
            public void finished(ConversionResult result) {
                throw new IllegalStateException("bad listener");
            }
        });
        registry.add(recorder("ok"));

        registry.finished(ConversionResult.converted("world", PATH, PATH, 1));

        assertEquals(List.of("ok:finished:world"), heard);
    }
}
