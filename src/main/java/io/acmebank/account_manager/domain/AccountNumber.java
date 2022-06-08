package io.acmebank.account_manager.domain;

public record AccountNumber(String value) {
    public AccountNumber {
        if(value == null || value.isBlank()) {
            throw new IllegalArgumentException("AccountNumber requires a non-null, non-empty value");
        }
    }
}
