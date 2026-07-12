package com.nexatek.invoice;

public final class InvoiceExtractionException extends Exception {

    public enum Reason {
        CONFIGURATION, INVALID_FILE, AUTHENTICATION, RATE_LIMITED, TIMEOUT, NETWORK, SERVICE
    }

    private final Reason reason;

    public InvoiceExtractionException(Reason reason, String safeMessage) {
        super(safeMessage);
        this.reason = reason;
    }

    public InvoiceExtractionException(Reason reason, String safeMessage, Throwable cause) {
        super(safeMessage, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
