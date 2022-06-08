package io.acmebank.account_manager.repository;

import io.acmebank.account_manager.repository.entity.Account;
import org.javamoney.moneta.FastMoney;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class AccountRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void shouldReturnAccountWhenQueriedByAccountNumber() {
        // given facts
        Account account = new Account(mockBalance());
        account.setAccountNumber("123456");
        entityManager.persist(account);
        entityManager.flush();

        // when
        Optional<Account> maybeAccount = accountRepository.findByAccountNumber("123456");

        // then
        assertTrue(maybeAccount.isPresent());
        maybeAccount.ifPresent(acc -> {
            assertEquals("123456", acc.getAccountNumber());
            assertTrue(mockBalance().isEqualTo( acc.getBalance()));
        });
    }

    private MonetaryAmount mockBalance() {
        return FastMoney.ofMinor(Monetary.getCurrency("HKD"), 10_000L);
    }
}