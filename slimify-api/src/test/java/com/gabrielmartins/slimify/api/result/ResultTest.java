package com.gabrielmartins.slimify.api.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ResultTest {

    private static final Path PATH = Path.of("world");

    @Test
    void failedResultsAreTheOnlyUnsuccessfulOnes() {
        assertTrue(ConversionResult.converted("world", PATH, PATH, 5).success());
        assertTrue(ConversionResult.skipped("world", PATH, PATH, "exists").success());
        assertFalse(ConversionResult.failed("world", PATH, PATH, 5, "boom").success());
    }

    @Test
    void reportCountsByStatus() {
        ConversionReport report = new ConversionReport(List.of(
                ConversionResult.converted("a", PATH, PATH, 10),
                ConversionResult.converted("b", PATH, PATH, 20),
                ConversionResult.skipped("c", PATH, PATH, "exists"),
                ConversionResult.failed("d", PATH, PATH, 5, "boom")));

        assertEquals(2, report.converted());
        assertEquals(1, report.skipped());
        assertEquals(1, report.failed());
        assertEquals(35, report.totalMillis());
        assertFalse(report.allSucceeded());
        assertEquals("2 converted, 1 skipped, 1 failed (35 ms)", report.summary());
    }

    @Test
    void emptyReportSucceeds() {
        ConversionReport report = new ConversionReport(List.of());

        assertTrue(report.empty());
        assertTrue(report.allSucceeded());
    }

    @Test
    void reportCannotBeChangedAfterwards() {
        List<ConversionResult> source = new ArrayList<>();
        ConversionReport report = new ConversionReport(source);
        source.add(ConversionResult.converted("a", PATH, PATH, 1));

        assertTrue(report.empty());
        assertThrows(UnsupportedOperationException.class, () -> report.results().add(ConversionResult.converted("a", PATH, PATH, 1)));
    }
}
