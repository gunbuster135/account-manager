package io.acmebank.account_manager.controller;

import io.acmebank.account_manager.controller.AccountController.AccountResponse;
import io.acmebank.account_manager.domain.AccountNumber;
import io.acmebank.account_manager.service.AccountService;
import io.acmebank.account_manager.service.NoAccountFoundException;
import org.javamoney.moneta.FastMoney;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.money.Monetary;
import javax.money.MonetaryAmount;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {
    @Mock
    AccountService accountService;

    @Test
    public void shouldReturn200WithBalanceForValidRequest() throws Exception {
        //given facts
        when(accountService.getBalance(new AccountNumber("123456")))
                .thenReturn(mockBalance());
        AccountController controller = new AccountController(accountService);

        //when
        ResponseEntity<AccountResponse> response = controller.getAccount("123456");

        //then
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getBalance(), mockBalance());
    }

    @Test
    public void shouldReturn404WithNoBodyForMissingAccountNumber() throws Exception {
        //given facts
        when(accountService.getBalance(new AccountNumber("123456")))
                .thenThrow(NoAccountFoundException.class);
        AccountController controller = new AccountController(accountService);

        //when
        ResponseEntity<AccountResponse> response = controller.getAccount("123456");

        //then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    private MonetaryAmount mockBalance() {
        return FastMoney.ofMinor(Monetary.getCurrency("HKD"), 10_000L);
    }
}