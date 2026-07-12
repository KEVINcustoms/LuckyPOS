package com.nexatek.invoice;

import java.util.List;

public record InvoiceValidationResult(List<ValidationMessage> messages) {
    public InvoiceValidationResult {
        messages = List.copyOf(messages);
    }

    public boolean hasBlockingErrors() {
        return messages.stream().anyMatch(message -> message.severity() == ValidationMessage.Severity.RED);
    }

    public long count(ValidationMessage.Severity severity) {
        return messages.stream().filter(message -> message.severity() == severity).count();
    }
}
