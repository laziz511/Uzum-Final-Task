package com.uzum.uzum_final_task.service;

import com.uzum.uzum_final_task.entity.Account;
import com.uzum.uzum_final_task.model.Currency;
import com.uzum.uzum_final_task.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final DecimalFormat decimalFormat = new DecimalFormat("#.######");
    private final AccountRepository accountRepository;
    private final CurrencyService currencyService;

    public void initializeAccounts() {
        createOrUpdateAccount("UZS", generateRandomAmount());
        List<Currency> currencies = currencyService.fetchCurrencies();
        currencies.forEach(currency -> createOrUpdateAccount(currency.getCcy(), generateRandomAmount()));
    }

    private void createOrUpdateAccount(String currencyName, Double amount) {
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

    private Double generateRandomAmount() {
        double randomValue = Math.random() * (50000 - 500) + 500;
        return Double.parseDouble(decimalFormat.format(randomValue));
    }
}
