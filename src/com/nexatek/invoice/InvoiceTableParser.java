package com.nexatek.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Header-driven supplier invoice table parser with arithmetic validation. */
public final class InvoiceTableParser {

    private static final Pattern PRODUCT_CODE = Pattern.compile("(?i)(?=.*[0-9])[a-z0-9][a-z0-9._/-]{1,29}");
    private static final Set<String> WAREHOUSE_OR_UNIT = Set.of(
            "FG", "WH", "EA", "PC", "PCS", "CTN", "BOX", "PKT", "UNIT", "UNITS");

    private static final Map<Field, Set<String>> HEADER_ALIASES = Map.of(
            Field.CODE, Set.of("code", "itemcode", "productcode", "stockcode", "sku", "itemno", "partno", "barcode"),
            Field.DESCRIPTION, Set.of("description", "itemdescription", "productdescription", "particulars", "product", "item"),
            Field.WAREHOUSE, Set.of("warehouse", "store", "location"),
            Field.QUANTITY, Set.of("shipquantity", "quantity", "qty", "deliveredqty", "delivered", "units", "packqty"),
            Field.UNIT_COST, Set.of("unitprice", "unitcost", "price", "rate", "cost"),
            Field.LINE_TOTAL, Set.of("grossamount", "lineamount", "linetotal", "netamount", "totalprice", "amount", "net", "total")
    );

    public InvoiceParseResult parseOcr(List<OcrPage> pages, String method) {
        List<InvoiceLineDraft> extracted = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        StringBuilder rawText = new StringBuilder();

        for (OcrPage page : pages == null ? List.<OcrPage>of() : pages) {
            rawText.append("--- Page ").append(page.getPageNumber()).append(" ---")
                    .append(System.lineSeparator()).append(page.getText()).append(System.lineSeparator());
            List<PositionedLine> lines = groupLines(page.getWords());
            HeaderLayout header = findHeader(lines);
            if (header == null) {
                warnings.add("Page " + page.getPageNumber() + ": table headings were not confidently located.");
                continue;
            }
            extracted.addAll(parsePositionedRows(lines, header));
        }

        InvoiceParseResult fallback = parseText(rawText.toString(), method + " (text fallback)", 72);
        if (extracted.isEmpty()) {
            List<String> combinedWarnings = new ArrayList<>(warnings);
            combinedWarnings.addAll(fallback.getWarnings());
            return new InvoiceParseResult(fallback.getLines(), rawText.toString(),
                    fallback.getExtractionMethod(), combinedWarnings);
        }
        InvoiceParseResult positioned = new InvoiceParseResult(extracted, rawText.toString(), method, warnings);
        // Perspective and irregular spacing can shift words into the wrong coordinate column.
        // Prefer the line-shaped interpretation when its arithmetic and confidence are stronger.
        boolean fallbackArithmeticVerified = !fallback.getLines().isEmpty()
                && fallback.getLines().size() >= positioned.getLines().size()
                && fallback.getLines().stream().allMatch(InvoiceLineDraft::totalsMatch);
        if (fallbackArithmeticVerified || fallback.qualityScore() > positioned.qualityScore()) {
            List<String> combinedWarnings = new ArrayList<>(warnings);
            combinedWarnings.add("Coordinate columns were unreliable; the arithmetic-validated text rows were used.");
            combinedWarnings.addAll(fallback.getWarnings());
            return new InvoiceParseResult(fallback.getLines(), rawText.toString(),
                    fallback.getExtractionMethod(), combinedWarnings);
        }
        return positioned;
    }

    public InvoiceParseResult parseText(String rawText, String method) {
        return parseText(rawText, method, 90);
    }

    private InvoiceParseResult parseText(String rawText, String method, int baseConfidence) {
        List<InvoiceLineDraft> lines = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String pendingDescription = "";
        boolean insideTable = false;

        for (String original : (rawText == null ? "" : rawText).split("\\R")) {
            String line = original.replace('|', ' ').replaceAll("\\s+", " ").trim();
            if (line.isBlank()) {
                continue;
            }
            if (isTableHeader(line)) {
                insideTable = true;
                pendingDescription = "";
                continue;
            }
            InvoiceLineDraft parsed = parseTextRow(line, baseConfidence);
            if (parsed != null) {
                if (!insideTable && parsed.getDescription().isBlank()) {
                    continue;
                }
                insideTable = true;
                if (!pendingDescription.isBlank()) {
                    parsed = withDescription(parsed, pendingDescription + " " + parsed.getDescription());
                    pendingDescription = "";
                }
                lines.add(parsed);
            } else if (insideTable && looksLikeDescriptionContinuation(line)) {
                pendingDescription = pendingDescription.isBlank() ? line : pendingDescription + " " + line;
            }
        }
        if (lines.isEmpty()) {
            warnings.add("No complete product rows were found. Verify that the table headings and prices are visible.");
        }
        return new InvoiceParseResult(lines, rawText, method, warnings);
    }

    private List<InvoiceLineDraft> parsePositionedRows(List<PositionedLine> lines, HeaderLayout header) {
        List<InvoiceLineDraft> results = new ArrayList<>();
        String pendingDescription = "";
        String pendingCode = "";

        for (int index = header.lineIndex + 1; index < lines.size(); index++) {
            PositionedLine line = lines.get(index);
            if (line.page != header.page) {
                break;
            }
            if (isFooterLine(line.text())) {
                continue;
            }

            EnumMap<Field, List<OcrWord>> cells = assignCells(line.words, header.anchors);
            String description = cellText(cells.get(Field.DESCRIPTION));
            String warehouse = cellText(cells.get(Field.WAREHOUSE));
            String code = cleanCode(cellText(cells.get(Field.CODE)));
            ParsedNumber quantity = parseNumber(cellText(cells.get(Field.QUANTITY)));
            ParsedNumber unitCost = parseNumber(cellText(cells.get(Field.UNIT_COST)));
            ParsedNumber lineTotal = parseNumber(cellText(cells.get(Field.LINE_TOTAL)));

            if (quantity.value == null || unitCost.value == null) {
                InvoiceLineDraft textFallback = parseTextRow(line.text(), averageConfidence(line.words));
                if (textFallback != null) {
                    results.add(textFallback);
                    pendingDescription = "";
                    pendingCode = "";
                } else if (!description.isBlank() && !isHeaderLike(description)) {
                    pendingDescription = pendingDescription.isBlank()
                            ? description : pendingDescription + " " + description;
                    if (!code.isBlank()) {
                        pendingCode = code;
                    }
                }
                continue;
            }

            if (!pendingDescription.isBlank()) {
                description = pendingDescription + " " + description;
                pendingDescription = "";
            }
            if (code.isBlank() && !pendingCode.isBlank()) {
                code = pendingCode;
                pendingCode = "";
            }
            if (description.isBlank()) {
                continue;
            }

            results.add(buildDraft(code, description, warehouse, quantity, unitCost, lineTotal,
                    averageConfidence(line.words)));
        }
        return results;
    }

    private InvoiceLineDraft parseTextRow(String line, int baseConfidence) {
        if (isMetadataLine(line) || isSummaryLine(line) || isFooterLine(line)) {
            return null;
        }

        String[] tokens = line.split("\\s+");
        List<NumericToken> numeric = new ArrayList<>();
        for (int index = 0; index < tokens.length; index++) {
            ParsedNumber number = parseNumber(tokens[index]);
            if (number.value != null && looksNumeric(tokens[index])) {
                numeric.add(new NumericToken(index, number));
            }
        }
        if (numeric.size() < 2) {
            return null;
        }

        NumericToken quantity;
        NumericToken cost;
        NumericToken total = null;
        if (numeric.size() >= 3) {
            total = numeric.get(numeric.size() - 1);
            cost = numeric.get(numeric.size() - 2);
            quantity = numeric.get(numeric.size() - 3);
        } else {
            cost = numeric.get(numeric.size() - 1);
            quantity = numeric.get(numeric.size() - 2);
        }
        if (quantity.index >= cost.index || quantity.number.value.signum() <= 0 || cost.number.value.signum() < 0) {
            return null;
        }

        String code = "";
        int descriptionStart = 0;
        if (tokens.length > 0 && looksLikeProductCode(tokens[0])) {
            code = cleanCode(tokens[0]);
            descriptionStart = 1;
        }
        int descriptionEnd = quantity.index;
        String warehouse = "";
        if (descriptionEnd > descriptionStart) {
            String trailing = tokens[descriptionEnd - 1];
            String normalizedTrailing = normalize(trailing).toUpperCase(Locale.ROOT);
            if (WAREHOUSE_OR_UNIT.contains(normalizedTrailing) && !looksLikeProductCode(trailing)) {
                warehouse = trailing;
            }
        }
        String description = join(tokens, descriptionStart, descriptionEnd);
        if (description.isBlank() || isHeaderLike(description) || isFooterLine(description)) {
            return null;
        }

        InvoiceLineDraft draft = buildDraft(code, description, warehouse, quantity.number,
                cost.number, total == null ? null : total.number, baseConfidence);
        // Text fallback keeps row-shaped values even if the line total is missing or the OCR confidence is low.
        // These rows are still surfaced to the review table and flagged for review instead of being dropped silently.
        return draft;
    }

    private InvoiceLineDraft buildDraft(String code, String description, String warehouse, ParsedNumber quantity,
            ParsedNumber unitCost, ParsedNumber lineTotal, int baseConfidence) {
        List<String> warnings = new ArrayList<>();
        int confidence = baseConfidence;
        if (code == null || code.isBlank()) {
            warnings.add("Product code was not visible; choose the matching product manually.");
            confidence -= 15;
        }
        boolean quantityCorrected = quantity != null && quantity.corrected;
        boolean unitCostCorrected = unitCost != null && unitCost.corrected;
        boolean lineTotalCorrected = lineTotal != null && lineTotal.corrected;
        if (quantityCorrected || unitCostCorrected || lineTotalCorrected) {
            warnings.add("OCR character corrections were applied to one or more numbers.");
            confidence -= 8;
        }

        BigDecimal totalValue = lineTotal == null || lineTotal.value == null ? null : lineTotal.value;
        if (lineTotal == null || lineTotal.value == null) {
            warnings.add("Line total was missing; review the row before receiving stock.");
            confidence -= 8;
        }

        InvoiceLineDraft initial = new InvoiceLineDraft(code, cleanDescription(description), warehouse, quantity == null ? null : quantity.value,
                unitCost == null ? null : unitCost.value, totalValue, confidence, warnings);
        if (!initial.hasWholeQuantity()) {
            warnings.add("Fractional quantity requires confirmation because inventory is stored as whole units.");
            confidence -= 20;
        }
        if (!initial.totalsMatch()) {
            warnings.add("Quantity x unit cost does not match the line total.");
            confidence -= 30;
        }
        return new InvoiceLineDraft(code, cleanDescription(description), warehouse, quantity == null ? null : quantity.value,
                unitCost == null ? null : unitCost.value, totalValue, confidence, warnings);
    }

    private List<PositionedLine> groupLines(List<OcrWord> words) {
        Map<String, List<OcrWord>> grouped = new LinkedHashMap<>();
        for (OcrWord word : words) {
            grouped.computeIfAbsent(word.lineKey(), key -> new ArrayList<>()).add(word);
        }
        List<PositionedLine> lines = new ArrayList<>();
        for (List<OcrWord> lineWords : grouped.values()) {
            lineWords.sort(Comparator.comparingInt(OcrWord::left));
            lines.add(new PositionedLine(lineWords));
        }
        lines.sort(Comparator.comparingInt((PositionedLine line) -> line.page)
                .thenComparingInt(line -> line.top).thenComparingInt(line -> line.left));
        return lines;
    }

    private HeaderLayout findHeader(List<PositionedLine> lines) {
        HeaderLayout best = null;
        int bestScore = 0;
        for (int index = 0; index < lines.size(); index++) {
            PositionedLine line = lines.get(index);
            EnumMap<Field, Double> anchors = detectAnchors(line.words);
            int score = anchors.size() * 2;
            if (anchors.containsKey(Field.DESCRIPTION)) {
                score += 3;
            }
            if (anchors.containsKey(Field.QUANTITY)) {
                score += 3;
            }
            if (anchors.containsKey(Field.UNIT_COST)) {
                score += 3;
            }
            if (anchors.containsKey(Field.LINE_TOTAL)) {
                score += 2;
            }
            if (score > bestScore && anchors.containsKey(Field.DESCRIPTION)
                    && anchors.containsKey(Field.QUANTITY) && anchors.containsKey(Field.UNIT_COST)) {
                best = new HeaderLayout(line.page, index, anchors);
                bestScore = score;
            }
        }
        return best;
    }

    private EnumMap<Field, Double> detectAnchors(List<OcrWord> words) {
        EnumMap<Field, Double> anchors = new EnumMap<>(Field.class);
        for (int start = 0; start < words.size(); start++) {
            StringBuilder phrase = new StringBuilder();
            for (int length = 1; length <= 3 && start + length <= words.size(); length++) {
                phrase.append(normalize(words.get(start + length - 1).text()));
                String candidate = phrase.toString();
                for (Map.Entry<Field, Set<String>> entry : HEADER_ALIASES.entrySet()) {
                    if (entry.getValue().contains(candidate)) {
                        int left = words.get(start).left();
                        int right = words.get(start + length - 1).right();
                        // Prefer specific multi-word aliases over a later generic match.
                        if (length > 1 || !anchors.containsKey(entry.getKey())) {
                            anchors.put(entry.getKey(), (left + right) / 2.0);
                        }
                    }
                }
            }
        }
        return anchors;
    }

    private EnumMap<Field, List<OcrWord>> assignCells(List<OcrWord> words, EnumMap<Field, Double> anchors) {
        List<Map.Entry<Field, Double>> ordered = new ArrayList<>(anchors.entrySet());
        ordered.sort(Map.Entry.comparingByValue());
        EnumMap<Field, List<OcrWord>> cells = new EnumMap<>(Field.class);
        for (Map.Entry<Field, Double> entry : ordered) {
            cells.put(entry.getKey(), new ArrayList<>());
        }
        for (OcrWord word : words) {
            Field nearest = null;
            double distance = Double.MAX_VALUE;
            for (Map.Entry<Field, Double> entry : ordered) {
                double candidate = Math.abs(word.centerX() - entry.getValue());
                if (candidate < distance) {
                    distance = candidate;
                    nearest = entry.getKey();
                }
            }
            if (nearest != null) {
                cells.get(nearest).add(word);
            }
        }
        return cells;
    }

    private ParsedNumber parseNumber(String source) {
        if (source == null || source.isBlank()) {
            return ParsedNumber.empty();
        }
        String original = source.trim();
        String candidate = original.replace('O', '0').replace('o', '0')
                .replace('I', '1').replace('l', '1').replace('|', '1')
                .replace("'", "").replace("’", "").replace(" ", "");
        boolean corrected = !candidate.equals(original.replace(" ", ""));
        boolean negativeParentheses = candidate.startsWith("(") && candidate.endsWith(")");
        candidate = candidate.replaceAll("[^0-9,.-]", "");
        if (negativeParentheses) {
            candidate = "-" + candidate;
        }
        if (!candidate.matches("-?\\d[\\d,.]*")) {
            return ParsedNumber.empty();
        }

        int comma = candidate.lastIndexOf(',');
        int dot = candidate.lastIndexOf('.');
        if (comma >= 0 && dot >= 0) {
            char decimal = comma > dot ? ',' : '.';
            char grouping = decimal == ',' ? '.' : ',';
            candidate = candidate.replace(String.valueOf(grouping), "");
            if (decimal == ',') {
                candidate = candidate.replace(',', '.');
            }
        } else if (comma >= 0) {
            int commaCount = count(candidate, ',');
            int digitsAfter = candidate.length() - comma - 1;
            if (commaCount == 1 && digitsAfter > 0 && digitsAfter <= 2) {
                candidate = candidate.replace(',', '.');
            } else {
                candidate = candidate.replace(",", "");
            }
        } else if (dot >= 0 && count(candidate, '.') > 1) {
            int digitsAfter = candidate.length() - dot - 1;
            if (digitsAfter > 0 && digitsAfter <= 2) {
                String integer = candidate.substring(0, dot).replace(".", "");
                candidate = integer + candidate.substring(dot);
            } else {
                candidate = candidate.replace(".", "");
            }
        }
        try {
            return new ParsedNumber(new BigDecimal(candidate), corrected);
        } catch (NumberFormatException ex) {
            return ParsedNumber.empty();
        }
    }

    private boolean looksNumeric(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String stripped = token.replaceAll("(?i)(UGX|USD|EUR|GBP|KES|TZS)", "")
                .replaceAll("[0-9OoIl|.,'’()\\-]", "");
        return stripped.isBlank() && token.matches(".*[0-9OoIl|].*");
    }

    private boolean looksLikeProductCode(String token) {
        String cleaned = token == null ? "" : token.replaceAll("^[^A-Za-z0-9]+|[^A-Za-z0-9._/-]+$", "");
        return PRODUCT_CODE.matcher(cleaned).matches();
    }

    private String cleanCode(String source) {
        if (source == null) {
            return "";
        }
        Matcher matcher = Pattern.compile("(?i)[a-z0-9][a-z0-9._/-]{1,29}").matcher(source);
        while (matcher.find()) {
            String candidate = matcher.group();
            if (candidate.matches(".*\\d.*")) {
                return candidate;
            }
        }
        return "";
    }

    private String cleanDescription(String description) {
        String cleaned = description == null ? "" : description.replaceAll("\\s+", " ").trim();
        if (cleaned.isBlank()) {
            return "";
        }
        return cleaned.replaceAll("[\\p{Punct}&&[^/._-]]+$", "").trim();
    }

    private boolean isFooterLine(String line) {
        String normalized = normalize(line);
        return normalized.matches("(subtotal|grandtotal|totalgrossamount|totalinvoice|totaltax|vat|tax|discount|freight|misccharges|amountdue|balance|paid|change|cash|items|sold|qty|quantity)[0-9]*")
                || normalized.contains("total") && normalized.contains("amount")
                || normalized.contains("amountdue")
                || normalized.contains("balance")
                || normalized.contains("itemsold")
                || normalized.contains("itemssold");
    }

    private boolean isSummaryLine(String line) {
        String normalized = normalize(line);
        if (normalized.isBlank()) {
            return false;
        }
        return normalized.contains("itemssold")
                || normalized.contains("itemssold")
                || normalized.contains("summary")
                || normalized.contains("totals")
                || normalized.contains("grandtotal")
                || normalized.matches("(items|sold|summary|total)[0-9]*");
    }

    private boolean isMetadataLine(String line) {
        String normalized = normalize(line);
        return normalized.matches(".*(invoicenumber|invoicedate|taxinvoice|customer|telephone|salesperson|purchaseorder|preparedby|printedon|printedat|receivedby|verificationcode|fiscaldocument|currency|shippingaddress|shippinginstructions).*?")
                || normalized.startsWith("fdn");
    }

    private boolean isHeaderLike(String text) {
        String normalized = normalize(text);
        return normalized.equals("description") || normalized.equals("itemdescription")
                || normalized.equals("productdescription") || normalized.equals("particulars");
    }

    private boolean isTableHeader(String line) {
        String normalized = normalize(line);
        boolean description = normalized.contains("description") || normalized.contains("particulars");
        boolean quantity = normalized.contains("quantity") || normalized.contains("qty");
        boolean price = normalized.contains("unitprice") || normalized.contains("unitcost")
                || normalized.contains("rate");
        return description && quantity && price;
    }

    private boolean looksLikeDescriptionContinuation(String line) {
        if (line.length() < 4 || isFooterLine(line) || isMetadataLine(line)) {
            return false;
        }
        long letters = line.chars().filter(Character::isLetter).count();
        long digits = line.chars().filter(Character::isDigit).count();
        return letters >= 4 && digits < 4 && !normalize(line).contains("warehouse");
    }

    private String cellText(List<OcrWord> words) {
        if (words == null || words.isEmpty()) {
            return "";
        }
        words.sort(Comparator.comparingInt(OcrWord::left));
        StringBuilder result = new StringBuilder();
        for (OcrWord word : words) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(word.text());
        }
        return result.toString();
    }

    private int averageConfidence(List<OcrWord> words) {
        return (int) Math.round(words.stream().mapToDouble(OcrWord::confidence).average().orElse(0));
    }

    private InvoiceLineDraft withDescription(InvoiceLineDraft source, String description) {
        return new InvoiceLineDraft(source.getProductCode(), cleanDescription(description), source.getWarehouse(), source.getQuantity(),
                source.getUnitCost(), source.getLineTotal(), source.getConfidence(), source.getWarnings());
    }

    private String join(String[] tokens, int start, int end) {
        StringBuilder value = new StringBuilder();
        for (int index = Math.max(0, start); index < Math.min(tokens.length, end); index++) {
            if (value.length() > 0) {
                value.append(' ');
            }
            value.append(tokens[index]);
        }
        return value.toString().trim();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private int count(String value, char character) {
        int count = 0;
        for (int index = 0; index < value.length(); index++) {
            if (value.charAt(index) == character) {
                count++;
            }
        }
        return count;
    }

    private enum Field {
        CODE, DESCRIPTION, WAREHOUSE, QUANTITY, UNIT_COST, LINE_TOTAL
    }

    private record ParsedNumber(BigDecimal value, boolean corrected) {
        static ParsedNumber empty() {
            return new ParsedNumber(null, false);
        }
    }

    private record NumericToken(int index, ParsedNumber number) {
    }

    private static final class PositionedLine {
        private final List<OcrWord> words;
        private final int page;
        private final int top;
        private final int left;

        private PositionedLine(List<OcrWord> words) {
            this.words = words;
            this.page = words.isEmpty() ? 0 : words.get(0).page();
            this.top = words.stream().mapToInt(OcrWord::top).min().orElse(0);
            this.left = words.stream().mapToInt(OcrWord::left).min().orElse(0);
        }

        private String text() {
            StringBuilder text = new StringBuilder();
            for (OcrWord word : words) {
                if (text.length() > 0) {
                    text.append(' ');
                }
                text.append(word.text());
            }
            return text.toString();
        }
    }

    private record HeaderLayout(int page, int lineIndex, EnumMap<Field, Double> anchors) {
    }
}
