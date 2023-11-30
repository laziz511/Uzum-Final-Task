package com.uzum.uzum_final_task.service.impl;

import com.uzum.uzum_final_task.entity.Account;
import com.uzum.uzum_final_task.model.Currency;
import com.uzum.uzum_final_task.repository.AccountRepository;
import com.uzum.uzum_final_task.service.AccountService;
import com.uzum.uzum_final_task.service.CommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final DecimalFormat decimalFormat = new DecimalFormat("#.###");
    private final AccountRepository accountRepository;
    private final CommissionService currencyService;

    public void initializeAccounts() {
        createOrUpdateAccount("UZS", generateRandomAmount());
        List<Currency> currencies = currencyService.fetchCurrencies();
        currencies.forEach(currency -> createOrUpdateAccount(currency.getCcy(), generateRandomAmount()));
    }

    private void createOrUpdateAccount(String currencyName, BigDecimal amount) {
        Optional<Account> existingAccount = accountRepository.findByCurrencyName(currencyName);

        Account account;
        if (existingAccount.isPresent()) {
            account = existingAccount.get();
            account.setAmount(amount);
        } else {
            account = new Account();
            account.setCurrencyName(currencyName);
            account.setAmount(amount);
        }
        accountRepository.save(account);
    }

    private BigDecimal generateRandomAmount() {
        double randomValue = Math.random() * (50000 - 500) + 500;
        return BigDecimal.valueOf(Double.parseDouble(decimalFormat.format(randomValue)));
    }
}
