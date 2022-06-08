package io.acmebank.account_manager.service;

import io.acmebank.account_manager.domain.AccountNumber;
import io.acmebank.account_manager.repository.AccountRepository;
import io.acmebank.account_manager.repository.entity.Account;
import org.javamoney.moneta.function.MonetaryQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;


public interface AccountService {

    MonetaryAmount getBalance(AccountNumber accountNumber) throws NoAccountFoundException;

    void transferMoney(AccountNumber sourceAccountNumber,
                       AccountNumber destinationAccountNumber,
                       MonetaryAmount amount) throws DestinationAccountNotFoundException, NoAccountFoundException, NotEnoughMoneyException;

    @Service
    class AccountServiceImpl implements AccountService {
        private AccountRepository accountRepository;

        @Autowired
        public AccountServiceImpl(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        @Override
        public MonetaryAmount getBalance(AccountNumber accountNumber) throws NoAccountFoundException {
            return accountRepository.findByAccountNumber(accountNumber.value())
                    .map(Account::getBalance)
                    .orElseThrow(() -> new NoAccountFoundException(accountNumber.value()));
        }

        @Override
        @Transactional
        public void transferMoney(AccountNumber sourceAccountNumber,
                                  AccountNumber destinationAccountNumber,
                                  MonetaryAmount amount) throws NoAccountFoundException, DestinationAccountNotFoundException, NotEnoughMoneyException {
            Account sourceAccount = accountRepository.findByAccountNumber(sourceAccountNumber.value())
                    .orElseThrow(() -> new NoAccountFoundException(sourceAccountNumber.value()));
            Account destinationAccount = accountRepository.findByAccountNumber(destinationAccountNumber.value())
                    .orElseThrow(() -> new DestinationAccountNotFoundException(destinationAccountNumber.value()));

            if (sourceAccount.getBalance().isLessThan(amount)) {
                throw new NotEnoughMoneyException();
            }

            MonetaryAmount sourceNewBalance = sourceAccount.getBalance().subtract(amount);
            MonetaryAmount destinationNewBalance = destinationAccount.getBalance().add(amount);

            sourceAccount.setBalance(toMinor(sourceNewBalance));
            destinationAccount.setBalance(toMinor(destinationNewBalance));

            accountRepository.saveAllAndFlush(List.of(sourceAccount, destinationAccount));
        }

        private BigDecimal toMinor(MonetaryAmount amount){
            return new BigDecimal(amount.query(MonetaryQueries.convertMinorPart()));
        }
    }
}
