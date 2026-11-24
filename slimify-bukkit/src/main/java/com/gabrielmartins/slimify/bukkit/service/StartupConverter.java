package com.gabrielmartins.slimify.bukkit.service;

import java.util.function.Supplier;

import com.gabrielmartins.slimify.api.Slimify;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class StartupConverter {

    private final Supplier<Slimify> slimify;
    private final ConsoleReporter reporter;

    public void run() {
        reporter.report(slimify.get().convertAll());
    }
}
