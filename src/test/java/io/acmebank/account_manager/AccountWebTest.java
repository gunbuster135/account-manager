package io.acmebank.account_manager;

import io.acmebank.account_manager.controller.AccountController.AccountResponse;
import io.acmebank.account_manager.controller.AccountController.AccountTransferRequest;
import io.acmebank.account_manager.controller.AccountController.ErrorResponse;
import io.acmebank.account_manager.controller.ApiError;
import io.acmebank.account_manager.repository.AccountRepository;
import io.acmebank.account_manager.repository.entity.Account;
import org.javamoney.moneta.FastMoney;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountWebTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository repository;

    @AfterEach
    public void tearDown() {
        repository.deleteAll();
    }

    @Test
    public void shouldReturnAccountWith200WhenCallingGetAccount() {
        //given facts
        repository.saveAndFlush(sampleAccount("123456"));

        //when
        ResponseEntity<AccountResponse> response =
                restTemplate.getForEntity("/accounts/123456", AccountResponse.class);


        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(mockBalance().isEqualTo(response.getBody().getBalance()));
    }

    @Test
    public void shouldReturn404ForMissingAccountWhenCallingGetAccount() {
        //when
        ResponseEntity<AccountResponse> response =
                restTemplate.getForEntity("/accounts/11111", AccountResponse.class);


        //then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void shouldReturn202ForSuccessfulTransferWhenCallingTransfer() {
        //given facts
        repository.saveAllAndFlush(
                List.of(
                        sampleAccount("123456"),
                        sampleAccount("111111")
                )
        );
        AccountTransferRequest request = new AccountTransferRequest("111111", mockBalance());

        //when
        ResponseEntity<Void> response =
                restTemplate.postForEntity("/accounts/123456/transfer", request, Void.TYPE);
        Optional<Account> sourceAccount = repository.findByAccountNumber("123456");
        Optional<Account> destinationAccount = repository.findByAccountNumber("111111");

        //then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        assertTrue(sourceAccount.isPresent());
        assertTrue(destinationAccount.isPresent());

        sourceAccount.ifPresent(account ->
                assertTrue(
                        account.getBalance()
                                .isEqualTo(FastMoney.ofMinor(Monetary.getCurrency("HKD"), 0L))
                )
        );

        destinationAccount.ifPresent(account ->
                assertTrue(
                        account.getBalance()
                                .isEqualTo(FastMoney.ofMinor(Monetary.getCurrency("HKD"), 20_000L))
                )
        );
    }

    @Test
    public void shouldReturn404ForTransferWhenSourceAccountIsMissing() {
        //given facts
        AccountTransferRequest request = new AccountTransferRequest("111111", mockBalance());

        //when
        ResponseEntity<Void> response =
                restTemplate.postForEntity("/accounts/123456/transfer", request, Void.TYPE);

        //then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void shouldReturn400WithNotEnoughFundErrorForFailedTransfer() {
        //given facts
        repository.saveAllAndFlush(
                List.of(
                        sampleAccount("123456"),
                        sampleAccount("111111")
                )
        );
        AccountTransferRequest request = new AccountTransferRequest("111111",
                FastMoney.ofMinor(Monetary.getCurrency("HKD"), 1000000L));

        //when
        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity("/accounts/123456/transfer", request, ErrorResponse.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApiError.NOT_ENOUGH_FUNDS.name(), response.getBody().getError());
        assertEquals(ApiError.NOT_ENOUGH_FUNDS.getValue(), response.getBody().getMessage());
    }

    @Test
    public void shouldReturn400WithDestinationAccountNotFoundForFailedTransfer() {
        //given facts
        repository.save(sampleAccount("123456"));
        AccountTransferRequest request = new AccountTransferRequest("111111",
                FastMoney.ofMinor(Monetary.getCurrency("HKD"), 1000000L));

        //when
        ResponseEntity<ErrorResponse> response =
                restTemplate.postForEntity("/accounts/123456/transfer", request, ErrorResponse.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ApiError.DESTINATION_ACCOUNT_NOT_FOUND.name(), response.getBody().getError());
        assertEquals(ApiError.DESTINATION_ACCOUNT_NOT_FOUND.getValue(), response.getBody().getMessage());
    }

    private Account sampleAccount(String accountNumber) {
        Account account = new Account(mockBalance());
        account.setAccountNumber(accountNumber);
        return account;
    }

    private MonetaryAmount mockBalance() {
        return FastMoney.ofMinor(Monetary.getCurrency("HKD"), 10_000L);
    }
}
