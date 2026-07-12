package com.nexatek.invoice;

/** One OCR word and its position on a page. */
public record OcrWord(String text, int page, int block, int paragraph, int line,
        int left, int top, int width, int height, double confidence) {

    public int right() {
        return left + width;
    }

    public int centerX() {
        return left + width / 2;
    }

    public int centerY() {
        return top + height / 2;
    }

    public String lineKey() {
        return page + ":" + block + ":" + paragraph + ":" + line;
    }
}
