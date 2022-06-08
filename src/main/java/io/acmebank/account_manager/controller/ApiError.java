package io.acmebank.account_manager.controller;

public enum ApiError {
    DESTINATION_ACCOUNT_NOT_FOUND("Destination account not found for transfer"),
    NOT_ENOUGH_FUNDS("Not enough funds to execute transaction");

    private final String value;

    ApiError(String value){
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
