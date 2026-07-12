package com.nexatek.invoice;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/** Offline OCR adapter around the installed Tesseract command-line program. */
public final class TesseractOcrEngine {

    private static final long PAGE_TIMEOUT_SECONDS = 120;
    private final String command;
    private final Path tessdataDirectory;

    public TesseractOcrEngine() {
        this.command = findTesseractCommand();
        this.tessdataDirectory = findTessdataDirectory(command);
    }

    public static boolean isInstalled() {
        return isExecutableFile(findTesseractCommand());
    }

    public OcrPage recognize(BufferedImage image, int pageNumber, int pageSegmentationMode)
            throws IOException, InterruptedException {
        Path temporaryImage = Files.createTempFile("luckypos-invoice-", ".png");
        try {
            if (!ImageIO.write(image, "png", temporaryImage.toFile())) {
                throw new IOException("Could not create the temporary OCR image.");
            }

            List<String> arguments = new ArrayList<>();
            arguments.add(command);
            arguments.add(temporaryImage.toAbsolutePath().toString());
            arguments.add("stdout");
            arguments.add("--oem");
            arguments.add("1");
            arguments.add("-l");
            arguments.add("eng");
            arguments.add("--psm");
            arguments.add(String.valueOf(pageSegmentationMode));
            arguments.add("--dpi");
            arguments.add("300");
            if (tessdataDirectory != null) {
                arguments.add("--tessdata-dir");
                arguments.add(tessdataDirectory.toString());
            }
            arguments.add("-c");
            arguments.add("preserve_interword_spaces=1");
            arguments.add("tsv");

            Process process = new ProcessBuilder(arguments).start();
            CompletableFuture<byte[]> stdout = readAsync(process.getInputStream());
            CompletableFuture<byte[]> stderr = readAsync(process.getErrorStream());

            boolean completed = process.waitFor(PAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new IOException("OCR timed out while reading page " + pageNumber + ".");
            }

            String tsv = decode(stdout, "OCR output");
            String diagnostics = decode(stderr, "OCR diagnostics").trim();
            if (process.exitValue() != 0) {
                throw new IOException(diagnostics.isBlank()
                        ? "Tesseract failed with exit code " + process.exitValue() + "."
                        : diagnostics);
            }
            OcrPage page = parseTsv(tsv, pageNumber, image.getWidth(), image.getHeight());
            if (page.getWords().isEmpty()) {
                throw new IOException(diagnostics.isBlank()
                        ? "No readable text was found on page " + pageNumber + "."
                        : diagnostics);
            }
            return page;
        } finally {
            Files.deleteIfExists(temporaryImage);
        }
    }

    public static OcrPage parseTsv(String tsv, int pageNumber, int width, int height) {
        List<OcrWord> words = new ArrayList<>();
        if (tsv != null) {
            String[] rows = tsv.split("\\R");
            for (int index = 1; index < rows.length; index++) {
                String[] cells = rows[index].split("\\t", 12);
                if (cells.length < 12 || !"5".equals(cells[0]) || cells[11].isBlank()) {
                    continue;
                }
                try {
                    double confidence = Double.parseDouble(cells[10]);
                    if (confidence < 0) {
                        continue;
                    }
                    words.add(new OcrWord(cells[11].trim(), pageNumber,
                            Integer.parseInt(cells[2]), Integer.parseInt(cells[3]), Integer.parseInt(cells[4]),
                            Integer.parseInt(cells[6]), Integer.parseInt(cells[7]),
                            Integer.parseInt(cells[8]), Integer.parseInt(cells[9]), confidence));
                } catch (NumberFormatException ignored) {
                    // A malformed TSV row is ignored while valid OCR words remain usable.
                }
            }
        }
        words.sort(Comparator.comparingInt(OcrWord::top).thenComparingInt(OcrWord::left));
        return new OcrPage(pageNumber, width, height, words, rebuildText(words));
    }

    private static String rebuildText(List<OcrWord> words) {
        Map<String, List<OcrWord>> lines = new LinkedHashMap<>();
        for (OcrWord word : words) {
            lines.computeIfAbsent(word.lineKey(), key -> new ArrayList<>()).add(word);
        }
        StringBuilder text = new StringBuilder();
        for (List<OcrWord> line : lines.values()) {
            line.sort(Comparator.comparingInt(OcrWord::left));
            int previousRight = -1;
            int averageHeight = (int) Math.max(1, line.stream().mapToInt(OcrWord::height).average().orElse(10));
            for (OcrWord word : line) {
                if (text.length() > 0 && previousRight >= 0) {
                    int gap = word.left() - previousRight;
                    text.append(gap > averageHeight * 2 ? "    " : " ");
                }
                text.append(word.text());
                previousRight = word.right();
            }
            text.append(System.lineSeparator());
        }
        return text.toString();
    }

    private static CompletableFuture<byte[]> readAsync(InputStream stream) {
        return CompletableFuture.supplyAsync(() -> {
            try (stream) {
                return stream.readAllBytes();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    private static String decode(CompletableFuture<byte[]> future, String label) throws IOException {
        try {
            return new String(future.get(10, TimeUnit.SECONDS), StandardCharsets.UTF_8);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(label + " was interrupted.", ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new IOException("Could not read " + label.toLowerCase() + ".", ex);
        }
    }

    private static String findTesseractCommand() {
        String configured = System.getProperty("luckypos.tesseract.path");
        if (isExecutableFile(configured)) {
            return configured.trim();
        }
        String environment = System.getenv("TESSERACT_PATH");
        if (isExecutableFile(environment)) {
            return environment.trim();
        }

        String pathValue = System.getenv("PATH");
        if (pathValue != null) {
            String executable = System.getProperty("os.name", "").toLowerCase().contains("win")
                    ? "tesseract.exe" : "tesseract";
            for (String folder : pathValue.split(Pattern.quote(File.pathSeparator))) {
                if (!folder.isBlank()) {
                    Path candidate = Paths.get(folder.trim(), executable);
                    if (Files.isRegularFile(candidate)) {
                        return candidate.toString();
                    }
                }
            }
        }

        String[] commonWindowsPaths = {
            "C:\\Program Files\\Tesseract-OCR\\tesseract.exe",
            "C:\\Program Files (x86)\\Tesseract-OCR\\tesseract.exe"
        };
        for (String path : commonWindowsPaths) {
            if (isExecutableFile(path)) {
                return path;
            }
        }
        return "tesseract";
    }

    private static Path findTessdataDirectory(String tesseractCommand) {
        try {
            Path executable = Paths.get(tesseractCommand).toAbsolutePath();
            Path parent = executable.getParent();
            if (parent != null && Files.isDirectory(parent.resolve("tessdata"))) {
                return parent.resolve("tessdata");
            }
        } catch (Exception ignored) {
        }
        String prefix = System.getenv("TESSDATA_PREFIX");
        if (prefix != null && Files.isDirectory(Paths.get(prefix))) {
            return Paths.get(prefix);
        }
        return null;
    }

    private static boolean isExecutableFile(String path) {
        return path != null && !path.isBlank() && Files.isRegularFile(Paths.get(path.trim()));
    }
}
