package com.nexatek.invoice;

public record SupplierChoice(int supplierId, String name) {
    @Override
    public String toString() {
        return supplierId + " - " + (name == null ? "" : name);
    }
}
