package com.nexatek.invoice;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Pure-Java preparation for photographed or scanned invoice pages. */
public final class InvoiceImagePreprocessor {

    private static final int TARGET_WIDTH = 2400;
    private static final int MAX_WIDTH = 3600;
    private static final long MAX_PIXELS = 25_000_000L;

    public PreparationResult prepare(BufferedImage source) {
        if (source == null || source.getWidth() < 100 || source.getHeight() < 100) {
            throw new IllegalArgumentException("The invoice image is empty or too small.");
        }

        List<String> warnings = new ArrayList<>();
        if (source.getWidth() < 1000) {
            warnings.add("The source image has low resolution; use a closer photo for better accuracy.");
        }

        BufferedImage normalized = toRgbOnWhite(source);
        normalized = scaleForOcr(normalized);
        BufferedImage gray = contrastStretch(toGray(normalized));

        double skew = estimateSkew(gray);
        if (Math.abs(skew) >= 0.4) {
            gray = rotate(gray, skew);
        }

        double sharpness = laplacianVariance(gray);
        if (sharpness < 70.0) {
            warnings.add("The photo appears blurred; verify low-confidence values carefully.");
        }

        BufferedImage adaptive = adaptiveThreshold(gray);
        double inkRatio = darkPixelRatio(adaptive);
        if (inkRatio < 0.002 || inkRatio > 0.40) {
            warnings.add("Lighting or page cropping is poor; retaking the photo may improve recognition.");
        }

        List<PreparedImage> candidates = List.of(
                new PreparedImage("adaptive", addWhiteBorder(adaptive, 18)),
                new PreparedImage("contrast", addWhiteBorder(gray, 18))
        );
        return new PreparationResult(candidates, warnings, skew, sharpness);
    }

    private BufferedImage toRgbOnWhite(BufferedImage source) {
        BufferedImage output = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, output.getWidth(), output.getHeight());
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return output;
    }

    private BufferedImage scaleForOcr(BufferedImage source) {
        double scale = 1.0;
        if (source.getWidth() < TARGET_WIDTH) {
            scale = Math.min(3.0, (double) TARGET_WIDTH / source.getWidth());
        } else if (source.getWidth() > MAX_WIDTH) {
            scale = (double) MAX_WIDTH / source.getWidth();
        }
        if (source.getWidth() * source.getHeight() * scale * scale > MAX_PIXELS) {
            scale = Math.sqrt((double) MAX_PIXELS / (source.getWidth() * (double) source.getHeight()));
        }
        if (Math.abs(scale - 1.0) < 0.01) {
            return source;
        }

        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return scaled;
    }

    private BufferedImage toGray(BufferedImage source) {
        BufferedImage gray = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = gray.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return gray;
    }

    private BufferedImage contrastStretch(BufferedImage gray) {
        int[] histogram = new int[256];
        for (int y = 0; y < gray.getHeight(); y += 2) {
            for (int x = 0; x < gray.getWidth(); x += 2) {
                histogram[gray.getRaster().getSample(x, y, 0)]++;
            }
        }
        int samples = 0;
        for (int count : histogram) {
            samples += count;
        }
        int low = percentile(histogram, samples, 0.02);
        int high = percentile(histogram, samples, 0.98);
        if (high - low < 40) {
            low = Math.max(0, low - 20);
            high = Math.min(255, high + 20);
        }

        BufferedImage output = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        double multiplier = 255.0 / Math.max(1, high - low);
        for (int y = 0; y < gray.getHeight(); y++) {
            for (int x = 0; x < gray.getWidth(); x++) {
                int value = gray.getRaster().getSample(x, y, 0);
                int stretched = (int) Math.round((value - low) * multiplier);
                output.getRaster().setSample(x, y, 0, Math.max(0, Math.min(255, stretched)));
            }
        }
        return output;
    }

    private int percentile(int[] histogram, int samples, double fraction) {
        int target = (int) Math.round(samples * fraction);
        int total = 0;
        for (int i = 0; i < histogram.length; i++) {
            total += histogram[i];
            if (total >= target) {
                return i;
            }
        }
        return 255;
    }

    private BufferedImage adaptiveThreshold(BufferedImage gray) {
        int width = gray.getWidth();
        int height = gray.getHeight();
        long[][] integral = new long[height + 1][width + 1];
        for (int y = 1; y <= height; y++) {
            long row = 0;
            for (int x = 1; x <= width; x++) {
                row += gray.getRaster().getSample(x - 1, y - 1, 0);
                integral[y][x] = integral[y - 1][x] + row;
            }
        }

        int radius = Math.max(16, Math.min(45, width / 70));
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height; y++) {
            int y1 = Math.max(0, y - radius);
            int y2 = Math.min(height - 1, y + radius);
            for (int x = 0; x < width; x++) {
                int x1 = Math.max(0, x - radius);
                int x2 = Math.min(width - 1, x + radius);
                long sum = integral[y2 + 1][x2 + 1] - integral[y1][x2 + 1]
                        - integral[y2 + 1][x1] + integral[y1][x1];
                int area = (x2 - x1 + 1) * (y2 - y1 + 1);
                int mean = (int) (sum / Math.max(1, area));
                int value = gray.getRaster().getSample(x, y, 0);
                output.getRaster().setSample(x, y, 0, value < mean - 11 ? 0 : 1);
            }
        }
        return output;
    }

    private double estimateSkew(BufferedImage gray) {
        double reduction = Math.min(1.0, 850.0 / gray.getWidth());
        int width = Math.max(1, (int) (gray.getWidth() * reduction));
        int height = Math.max(1, (int) (gray.getHeight() * reduction));
        BufferedImage sample = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = sample.createGraphics();
        graphics.drawImage(gray, 0, 0, width, height, null);
        graphics.dispose();

        double bestAngle = 0.0;
        double bestScore = -1.0;
        for (double angle = -4.0; angle <= 4.001; angle += 0.5) {
            double radians = Math.toRadians(angle);
            double sin = Math.sin(radians);
            double cos = Math.cos(radians);
            int[] rows = new int[height + width / 8 + 4];
            for (int y = 2; y < height - 2; y += 2) {
                for (int x = 2; x < width - 2; x += 2) {
                    if (sample.getRaster().getSample(x, y, 0) < 130) {
                        int row = (int) Math.round(y * cos + x * sin) + width / 16;
                        if (row >= 0 && row < rows.length) {
                            rows[row]++;
                        }
                    }
                }
            }
            double score = 0.0;
            for (int count : rows) {
                score += (double) count * count;
            }
            if (score > bestScore) {
                bestScore = score;
                bestAngle = angle;
            }
        }
        return Math.abs(bestAngle) < 0.4 ? 0.0 : bestAngle;
    }

    private BufferedImage rotate(BufferedImage source, double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int width = (int) Math.floor(source.getWidth() * cos + source.getHeight() * sin);
        int height = (int) Math.floor(source.getHeight() * cos + source.getWidth() * sin);
        BufferedImage rotated = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = rotated.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        AffineTransform transform = new AffineTransform();
        transform.translate((width - source.getWidth()) / 2.0, (height - source.getHeight()) / 2.0);
        transform.rotate(radians, source.getWidth() / 2.0, source.getHeight() / 2.0);
        graphics.drawRenderedImage(source, transform);
        graphics.dispose();
        return rotated;
    }

    private double laplacianVariance(BufferedImage gray) {
        double sum = 0.0;
        double squareSum = 0.0;
        int count = 0;
        int step = Math.max(1, gray.getWidth() / 1200);
        for (int y = step; y < gray.getHeight() - step; y += step) {
            for (int x = step; x < gray.getWidth() - step; x += step) {
                int center = gray.getRaster().getSample(x, y, 0);
                int laplacian = 4 * center
                        - gray.getRaster().getSample(x - step, y, 0)
                        - gray.getRaster().getSample(x + step, y, 0)
                        - gray.getRaster().getSample(x, y - step, 0)
                        - gray.getRaster().getSample(x, y + step, 0);
                sum += laplacian;
                squareSum += (double) laplacian * laplacian;
                count++;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        double mean = sum / count;
        return squareSum / count - mean * mean;
    }

    private double darkPixelRatio(BufferedImage image) {
        long dark = 0;
        long total = 0;
        for (int y = 0; y < image.getHeight(); y += 3) {
            for (int x = 0; x < image.getWidth(); x += 3) {
                if (image.getRaster().getSample(x, y, 0) == 0) {
                    dark++;
                }
                total++;
            }
        }
        return total == 0 ? 0.0 : (double) dark / total;
    }

    private BufferedImage addWhiteBorder(BufferedImage source, int border) {
        int type = source.getType() == BufferedImage.TYPE_BYTE_BINARY
                ? BufferedImage.TYPE_BYTE_BINARY : BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage output = new BufferedImage(source.getWidth() + border * 2,
                source.getHeight() + border * 2, type);
        Graphics2D graphics = output.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, output.getWidth(), output.getHeight());
        graphics.drawImage(source, border, border, null);
        graphics.dispose();
        return output;
    }

    public record PreparedImage(String name, BufferedImage image) {
    }

    public record PreparationResult(List<PreparedImage> candidates, List<String> warnings,
            double correctedSkewDegrees, double sharpness) {
    }
}
