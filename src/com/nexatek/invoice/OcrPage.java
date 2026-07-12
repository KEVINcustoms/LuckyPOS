package com.nexatek.invoice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** OCR words for one page, with the text representation retained for auditing. */
public final class OcrPage {

    private final int pageNumber;
    private final int width;
    private final int height;
    private final List<OcrWord> words;
    private final String text;

    public OcrPage(int pageNumber, int width, int height, List<OcrWord> words, String text) {
        this.pageNumber = pageNumber;
        this.width = width;
        this.height = height;
        this.words = Collections.unmodifiableList(new ArrayList<>(words));
        this.text = text == null ? "" : text;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<OcrWord> getWords() {
        return words;
    }

    public String getText() {
        return text;
    }
}
