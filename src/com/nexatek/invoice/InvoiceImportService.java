package com.nexatek.invoice;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

/** Facade that loads images/PDFs, performs OCR, and returns validated invoice rows. */
public final class InvoiceImportService {

    private static final long MAX_FILE_BYTES = 50L * 1024L * 1024L;
    private static final int MAX_PAGES = 20;
    private static final float PDF_RENDER_DPI = 300f;

    private final InvoiceImagePreprocessor preprocessor;
    private final TesseractOcrEngine ocr;
    private final InvoiceTableParser parser;

    public InvoiceImportService() {
        this(new InvoiceImagePreprocessor(), new TesseractOcrEngine(), new InvoiceTableParser());
    }

    InvoiceImportService(InvoiceImagePreprocessor preprocessor, TesseractOcrEngine ocr,
            InvoiceTableParser parser) {
        this.preprocessor = preprocessor;
        this.ocr = ocr;
        this.parser = parser;
    }

    public InvoiceParseResult extract(Path source) throws IOException, InterruptedException {
        validateSource(source);
        String name = source.getFileName().toString().toLowerCase();
        if (name.endsWith(".txt")) {
            return parser.parseText(Files.readString(source, StandardCharsets.UTF_8), "Text invoice");
        }
        if (isPdf(source)) {
            return extractPdf(source);
        }
        return extractImage(source);
    }

    private InvoiceParseResult extractPdf(Path source) throws IOException, InterruptedException {
        try (PDDocument document = Loader.loadPDF(source.toFile())) {
            if (document.isEncrypted()) {
                throw new IOException("Password-protected invoices are not supported. Save an unlocked copy first.");
            }
            int pages = document.getNumberOfPages();
            if (pages < 1) {
                throw new IOException("The PDF contains no pages.");
            }
            if (pages > MAX_PAGES) {
                throw new IOException("The PDF has " + pages + " pages; the safety limit is " + MAX_PAGES + ".");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String embeddedText = stripper.getText(document);
            if (embeddedText != null && embeddedText.replaceAll("\\s", "").length() >= 40) {
                InvoiceParseResult nativeResult = parser.parseText(embeddedText, "PDF embedded text");
                if (!nativeResult.getLines().isEmpty()
                        && nativeResult.getLines().stream().allMatch(InvoiceLineDraft::totalsMatch)) {
                    return nativeResult;
                }
            }

            PDFRenderer renderer = new PDFRenderer(document);
            List<BufferedImage> renderedPages = new ArrayList<>();
            for (int page = 0; page < pages; page++) {
                renderedPages.add(renderer.renderImageWithDPI(page, PDF_RENDER_DPI, ImageType.RGB));
            }
            InvoiceParseResult ocrResult = extractImages(renderedPages, "Scanned PDF OCR");
            if (!ocrResult.getLines().isEmpty()) {
                return ocrResult;
            }
            if (embeddedText != null && !embeddedText.isBlank()) {
                return parser.parseText(embeddedText, "PDF embedded text (fallback)");
            }
            return ocrResult;
        } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException ex) {
            throw new IOException("Password-protected invoices are not supported. Save an unlocked copy first.", ex);
        }
    }

    private InvoiceParseResult extractImage(Path source) throws IOException, InterruptedException {
        List<BufferedImage> pages = readImagePages(source);
        if (pages.isEmpty()) {
            throw new IOException("This image format could not be read. Use PNG, JPG, BMP, or TIFF.");
        }
        return extractImages(pages, "Photo/image OCR");
    }

    InvoiceParseResult extractImages(List<BufferedImage> images, String method)
            throws IOException, InterruptedException {
        if (images == null || images.isEmpty()) {
            throw new IOException("The invoice contains no readable pages.");
        }
        if (images.size() > MAX_PAGES) {
            throw new IOException("The invoice exceeds the " + MAX_PAGES + " page safety limit.");
        }

        List<String> preparationWarnings = new ArrayList<>();
        List<InvoiceImagePreprocessor.PreparationResult> preparedPages = new ArrayList<>();
        List<OcrPage> primaryPages = new ArrayList<>();
        IOException lastOcrFailure = null;
        for (int index = 0; index < images.size(); index++) {
            int pageNumber = index + 1;
            InvoiceImagePreprocessor.PreparationResult prepared = preprocessor.prepare(images.get(index));
            preparedPages.add(prepared);
            preparationWarnings.addAll(prepared.warnings().stream()
                    .map(warning -> "Page " + pageNumber + ": " + warning).toList());
            try {
                primaryPages.add(ocr.recognize(prepared.candidates().get(0).image(), pageNumber, 6));
            } catch (IOException ex) {
                lastOcrFailure = ex;
                preparationWarnings.add("Page " + pageNumber + ": primary OCR mode found no usable text; retrying.");
            }
        }

        InvoiceParseResult primary = primaryPages.isEmpty()
                ? new InvoiceParseResult(List.of(), "", method + " (layout mode)", List.of())
                : parser.parseOcr(primaryPages, method + " (layout mode)");
        InvoiceParseResult best = primary;
        if (primary.getLines().isEmpty() || primary.qualityScore() < 90) {
            List<OcrPage> fallbackPages = new ArrayList<>();
            for (int index = 0; index < preparedPages.size(); index++) {
                InvoiceImagePreprocessor.PreparationResult prepared = preparedPages.get(index);
                try {
                    // PSM 4 keeps invoice columns and rows together better than sparse-text mode.
                    fallbackPages.add(ocr.recognize(prepared.candidates().get(1).image(), index + 1, 4));
                } catch (IOException ex) {
                    lastOcrFailure = ex;
                    preparationWarnings.add("Page " + (index + 1) + ": fallback OCR mode found no usable text.");
                }
            }
            InvoiceParseResult fallback = fallbackPages.isEmpty()
                    ? new InvoiceParseResult(List.of(), "", method + " (contrast table mode)", List.of())
                    : parser.parseOcr(fallbackPages, method + " (contrast table mode)");
            if (fallback.qualityScore() > primary.qualityScore()) {
                best = fallback;
            }
        }

        // Difficult phone photographs can have disconnected columns. Sparse mode is slower,
        // so it is only attempted when the two row-preserving passes remain weak.
        if (best.getLines().isEmpty() || best.qualityScore() < 80) {
            List<OcrPage> sparsePages = new ArrayList<>();
            for (int index = 0; index < preparedPages.size(); index++) {
                InvoiceImagePreprocessor.PreparationResult prepared = preparedPages.get(index);
                try {
                    sparsePages.add(ocr.recognize(prepared.candidates().get(1).image(), index + 1, 11));
                } catch (IOException ex) {
                    lastOcrFailure = ex;
                    preparationWarnings.add("Page " + (index + 1) + ": sparse OCR mode found no usable text.");
                }
            }
            InvoiceParseResult sparse = sparsePages.isEmpty()
                    ? new InvoiceParseResult(List.of(), "", method + " (contrast sparse mode)", List.of())
                    : parser.parseOcr(sparsePages, method + " (contrast sparse mode)");
            if (sparse.qualityScore() > best.qualityScore()
                    || best.getRawText().isBlank() && !sparse.getRawText().isBlank()) {
                best = sparse;
            }
        }

        List<String> warnings = new ArrayList<>(preparationWarnings);
        warnings.addAll(best.getWarnings());
        if (best.getLines().isEmpty()) {
            if (primaryPages.isEmpty() && lastOcrFailure != null) {
                throw new IOException("No readable invoice text was found after both OCR modes.", lastOcrFailure);
            }
            warnings.add("No safe product rows were identified; use a flatter, closer photo showing the whole table.");
        }
        return new InvoiceParseResult(best.getLines(), best.getRawText(), best.getExtractionMethod(), warnings);
    }

    private List<BufferedImage> readImagePages(Path source) throws IOException {
        List<BufferedImage> pages = new ArrayList<>();
        try (ImageInputStream input = ImageIO.createImageInputStream(source.toFile())) {
            if (input == null) {
                return pages;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                return pages;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, false, true);
                int count;
                try {
                    count = reader.getNumImages(true);
                } catch (IOException ex) {
                    count = 1;
                }
                if (count > MAX_PAGES) {
                    throw new IOException("The image contains more than " + MAX_PAGES + " pages.");
                }
                for (int index = 0; index < count; index++) {
                    pages.add(reader.read(index));
                }
            } finally {
                reader.dispose();
            }
        }
        return pages;
    }

    private boolean isPdf(Path source) throws IOException {
        byte[] header = new byte[5];
        try (var stream = Files.newInputStream(source)) {
            int read = stream.read(header);
            return read == header.length && new String(header, StandardCharsets.US_ASCII).equals("%PDF-");
        }
    }

    private void validateSource(Path source) throws IOException {
        if (source == null || !Files.isRegularFile(source)) {
            throw new IOException("Choose an existing invoice file.");
        }
        long size = Files.size(source);
        if (size <= 0) {
            throw new IOException("The selected invoice file is empty.");
        }
        if (size > MAX_FILE_BYTES) {
            throw new IOException("The invoice is larger than the 50 MB safety limit.");
        }
    }
}
