package com.nexatek.invoice;

public record ValidationMessage(Severity severity, String code, String message, Integer itemIndex) {
    public enum Severity { GREEN, YELLOW, RED }
}
