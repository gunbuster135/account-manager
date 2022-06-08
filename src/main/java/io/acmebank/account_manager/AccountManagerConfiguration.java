package io.acmebank.account_manager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.jackson.datatype.money.MoneyModule;

@Configuration
public class AccountManagerConfiguration {

    // jackson money module for JSON de/serialization of MonetaryAmount
    @Bean
    public MoneyModule moneyModule() {
        return new MoneyModule().withDefaultFormatting();
    }
}
