package io.acmebank.account_manager.service;

public class NoAccountFoundException extends Exception {

    public NoAccountFoundException(String accountNumber) {
        super(String.format("No account found for account number '%s'", accountNumber));
    }
}
