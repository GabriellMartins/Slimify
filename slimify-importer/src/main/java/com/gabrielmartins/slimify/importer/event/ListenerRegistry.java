package com.gabrielmartins.slimify.importer.event;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;

import com.gabrielmartins.slimify.api.event.ConversionListener;
import com.gabrielmartins.slimify.api.result.ConversionResult;

import lombok.extern.java.Log;

@Log
public final class ListenerRegistry {

    private final List<ConversionListener> listeners = new CopyOnWriteArrayList<>();

    public void add(ConversionListener listener) {
        listeners.add(listener);
    }

    public void remove(ConversionListener listener) {
        listeners.remove(listener);
    }

    public void started(String world, Path source, Path target) {
        dispatch(listener -> listener.started(world, source, target));
    }

    public void finished(ConversionResult result) {
        dispatch(listener -> listener.finished(result));
    }

    private void dispatch(Consumer<ConversionListener> action) {
        for (ConversionListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (RuntimeException e) {
                log.log(Level.WARNING, "A conversion listener failed", e);
            }
        }
    }
}
