package io.acmebank.account_manager.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.acmebank.account_manager.domain.AccountNumber;
import io.acmebank.account_manager.service.AccountService;
import io.acmebank.account_manager.service.DestinationAccountNotFoundException;
import io.acmebank.account_manager.service.NoAccountFoundException;
import io.acmebank.account_manager.service.NotEnoughMoneyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.money.MonetaryAmount;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        try {
            return ResponseEntity.ok(
                    new AccountResponse(accountService.getBalance(new AccountNumber(accountNumber)))
            );
        } catch (NoAccountFoundException e) {
            return ResponseEntity.notFound()
                    .build();
        }
    }

    @PostMapping("/{accountNumber}/transfer")
    public ResponseEntity<?> transfer(@PathVariable String accountNumber,
                                      @Validated @RequestBody AccountTransferRequest request) {
        try {
            accountService.transferMoney(new AccountNumber(accountNumber),
                    new AccountNumber(request.getDestinationAccountNumber()),
                    request.getAmount());
            return ResponseEntity.noContent().build();
        } catch (DestinationAccountNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(ApiError.DESTINATION_ACCOUNT_NOT_FOUND));
        } catch (NoAccountFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (NotEnoughMoneyException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(ApiError.NOT_ENOUGH_FUNDS));
        }
    }

    public static class ErrorResponse {
        private String error;
        private String message;

        public ErrorResponse(ApiError apiError) {
            this.error = apiError.name();
            this.message = apiError.getValue();
        }

        //for testing, not used
        @JsonCreator
        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class AccountTransferRequest {
        @NotBlank
        private String destinationAccountNumber;
        @NotNull
        private MonetaryAmount amount;

        @JsonCreator
        public AccountTransferRequest(String destinationAccountNumber, MonetaryAmount amount) {
            this.destinationAccountNumber = destinationAccountNumber;
            this.amount = amount;
        }

        public String getDestinationAccountNumber() {
            return destinationAccountNumber;
        }

        public MonetaryAmount getAmount() {
            return amount;
        }

    }

    public static class AccountResponse {
        private MonetaryAmount balance;

        @JsonCreator
        public AccountResponse(MonetaryAmount balance) {
            this.balance = balance;
        }

        public MonetaryAmount getBalance() {
            return balance;
        }
    }
}
