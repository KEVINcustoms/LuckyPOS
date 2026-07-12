package com.nexatek.invoice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Complete extraction result shown to the user for review. */
public final class InvoiceParseResult {

    private final List<InvoiceLineDraft> lines;
    private final String rawText;
    private final String extractionMethod;
    private final List<String> warnings;

    public InvoiceParseResult(List<InvoiceLineDraft> lines, String rawText,
            String extractionMethod, List<String> warnings) {
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines == null ? List.of() : lines));
        this.rawText = rawText == null ? "" : rawText;
        this.extractionMethod = extractionMethod == null ? "Unknown" : extractionMethod;
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings == null ? List.of() : warnings));
    }

    public List<InvoiceLineDraft> getLines() {
        return lines;
    }

    public String getRawText() {
        return rawText;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public BigDecimal getComputedSubtotal() {
        return lines.stream()
                .map(InvoiceLineDraft::getLineTotal)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getReadyCount() {
        return (int) lines.stream().filter(line -> "READY".equals(line.getStatus())).count();
    }

    public int qualityScore() {
        if (lines.isEmpty()) {
            return 0;
        }
        double average = lines.stream().mapToInt(InvoiceLineDraft::getConfidence).average().orElse(0);
        long valid = lines.stream().filter(InvoiceLineDraft::totalsMatch).count();
        return Math.min(100, (int) Math.round(
                average + (valid * 12.0 / lines.size()) + Math.min(8, lines.size())));
    }
}
