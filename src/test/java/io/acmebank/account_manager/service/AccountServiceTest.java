package io.acmebank.account_manager.service;

import io.acmebank.account_manager.domain.AccountNumber;
import io.acmebank.account_manager.repository.AccountRepository;
import io.acmebank.account_manager.repository.entity.Account;
import org.javamoney.moneta.FastMoney;
import org.javamoney.moneta.function.MonetaryQueries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    AccountRepository accountRepository;

    @Test
    public void shouldReturnBalanceForAccount() throws Exception {
        //given facts
        AccountNumber accountNumber = new AccountNumber("123456");
        when(accountRepository.findByAccountNumber(eq(accountNumber.value())))
                .thenReturn(Optional.of(new Account(mockBalance())));

        //when
        AccountService service = new AccountService.AccountServiceImpl(accountRepository);
        var balance = service.getBalance(new AccountNumber("123456"));

        //then
        assertEquals(10_000L, balance.query(MonetaryQueries.convertMinorPart()));
        assertEquals("HKD", balance.getCurrency().getCurrencyCode());
    }

    @Test
    public void shouldThrowNoAccountFoundWhenQueryReturnsNoResults() {
        //given facts
        AccountNumber accountNumber = new AccountNumber("123456");
        when(accountRepository.findByAccountNumber(eq(accountNumber.value())))
                .thenReturn(Optional.empty());

        //when
        AccountService service = new AccountService.AccountServiceImpl(accountRepository);

        //then
        assertThrows(NoAccountFoundException.class, () -> service.getBalance(accountNumber));
    }

    @Test
    public void shouldTransferAmountCorrectlyFromOneAccountToAnotherAccount() throws Exception{
        //given facts
        Account sourceAccount = new Account(mockBalance());
        Account destinationAccount = new Account(mockBalance());
        AccountNumber sourceAccountNumber = new AccountNumber("123456");
        AccountNumber destinationAccountNumber = new AccountNumber("111111");
        when(accountRepository.findByAccountNumber(eq(sourceAccountNumber.value())))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(eq(destinationAccountNumber.value())))
                .thenReturn(Optional.of(destinationAccount));

        //when
        AccountService service = new AccountService.AccountServiceImpl(accountRepository);
        service.transferMoney(
                sourceAccountNumber,
                destinationAccountNumber,
                FastMoney.ofMinor(Monetary.getCurrency("HKD"), 50L)
        );

        //then
        assertEquals(9950L, accountAmount(sourceAccount));
        assertEquals(10050L, accountAmount(destinationAccount));
        verify(accountRepository, times(1)).saveAllAndFlush(anyList());
    }

    @Test
    public void shouldThrowNoAccountNotFoundForMissingSourceAccount() {
        //given facts
        AccountNumber sourceAccountNumber = new AccountNumber("123456");
        when(accountRepository.findByAccountNumber(eq(sourceAccountNumber.value())))
                .thenReturn(Optional.empty());

        //when
        AccountService service = new AccountService.AccountServiceImpl(accountRepository);

        //then
        assertThrows(NoAccountFoundException.class, () -> service.transferMoney(
                sourceAccountNumber,
                new AccountNumber("123123"),
                FastMoney.ofMinor(Monetary.getCurrency("HKD"), 50L)
        ));
    }

    @Test
    public void shouldThrowNotEnoughMoneyForNotSufficientBalance() {
        //given facts
        Account sourceAccount = new Account(FastMoney.of(0L, "HKD"));
        Account destinationAccount = new Account(mockBalance());
        AccountNumber sourceAccountNumber = new AccountNumber("123456");
        AccountNumber destinationAccountNumber = new AccountNumber("111111");
        when(accountRepository.findByAccountNumber(eq(sourceAccountNumber.value())))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(eq(destinationAccountNumber.value())))
                .thenReturn(Optional.of(destinationAccount));

        //when
        AccountService service = new AccountService.AccountServiceImpl(accountRepository);

        //then
        assertThrows(NotEnoughMoneyException.class, () -> service.transferMoney(
                sourceAccountNumber,
                new AccountNumber("111111"),
                FastMoney.ofMinor(Monetary.getCurrency("HKD"), 50L)
        ));
    }

    @Test
    public void shouldThrowDestinationAccountNotFoundForMissingDestinationAccount() {
        //given facts
        Account sourceAccount = new Account(FastMoney.of(0L, "HKD"));
        AccountNumber sourceAccountNumber = new AccountNumber("123456");
        AccountNumber destinationAccountNumber = new AccountNumber("111111");
        when(accountRepository.findByAccountNumber(eq(sourceAccountNumber.value())))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(eq(destinationAccountNumber.value())))
                .thenReturn(Optional.empty());

        //when
        AccountService service = new AccountService.AccountServiceImpl(accountRepository);

        //then
        assertThrows(DestinationAccountNotFoundException.class, () -> service.transferMoney(
                sourceAccountNumber,
                new AccountNumber("111111"),
                FastMoney.ofMinor(Monetary.getCurrency("HKD"), 50L)
        ));
    }

    private long accountAmount(Account account){
        return account.getBalance().query(MonetaryQueries.convertMinorPart());
    }

    private MonetaryAmount mockBalance() {
        return FastMoney.ofMinor(Monetary.getCurrency("HKD"), 10_000L);
    }
}