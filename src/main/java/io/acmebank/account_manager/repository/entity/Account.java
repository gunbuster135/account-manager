package io.acmebank.account_manager.repository.entity;

import org.javamoney.moneta.FastMoney;
import org.javamoney.moneta.function.MonetaryQueries;
import org.springframework.lang.NonNull;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @NotBlank
    @Column(name = "currency", nullable = false)
    private String currency;

    @NotNull
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    public Account(@NonNull MonetaryAmount startingBalance) {
        this.currency = startingBalance.getCurrency().getCurrencyCode();
        this.balance = new BigDecimal(startingBalance.query(MonetaryQueries.convertMinorPart()));
    }

    public Account() {

    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }


    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }


    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public MonetaryAmount getBalance() {
        return FastMoney.ofMinor(Monetary.getCurrency(currency), balance.longValue());
    }
}
