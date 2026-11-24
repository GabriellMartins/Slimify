package com.gabrielmartins.slimify.bukkit.service;

import java.util.logging.Logger;

import com.gabrielmartins.slimify.api.result.ConversionReport;
import com.gabrielmartins.slimify.api.result.ConversionResult;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ConsoleReporter {

    private final Logger logger;

    public void report(ConversionReport report) {
        if (report.empty()) {
            logger.info("No world folders to convert");
            return;
        }
        report.results().forEach(this::report);
        logger.info(report.summary());
    }

    private void report(ConversionResult result) {
        switch (result.status()) {
            case CONVERTED -> logger.info(result.world() + " -> " + result.target().getFileName() + " (" + result.elapsedMillis() + " ms)");
            case SKIPPED -> logger.info(result.world() + " skipped: " + result.detail());
            case FAILED -> logger.warning(result.world() + " failed: " + result.detail());
        }
    }
}
