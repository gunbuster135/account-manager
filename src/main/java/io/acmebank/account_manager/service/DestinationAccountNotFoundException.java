package io.acmebank.account_manager.service;

public class DestinationAccountNotFoundException extends Exception {
    public DestinationAccountNotFoundException(String accountNumber) {
        super(String.format("Destination account not found when executing transfer, account number '%s'", accountNumber));
    }
}
