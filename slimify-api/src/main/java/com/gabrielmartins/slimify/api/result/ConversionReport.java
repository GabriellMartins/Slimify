package com.gabrielmartins.slimify.api.result;

import java.util.List;

public record ConversionReport(List<ConversionResult> results) {

    public ConversionReport {
        results = List.copyOf(results);
    }

    public long converted() {
        return count(ConversionStatus.CONVERTED);
    }

    public long skipped() {
        return count(ConversionStatus.SKIPPED);
    }

    public long failed() {
        return count(ConversionStatus.FAILED);
    }

    public long totalMillis() {
        return results.stream().mapToLong(ConversionResult::elapsedMillis).sum();
    }

    public boolean allSucceeded() {
        return failed() == 0;
    }

    public boolean empty() {
        return results.isEmpty();
    }

    public String summary() {
        return converted() + " converted, " + skipped() + " skipped, " + failed() + " failed (" + totalMillis() + " ms)";
    }

    private long count(ConversionStatus status) {
        return results.stream().filter(result -> result.status() == status).count();
    }
}
