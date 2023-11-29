package com.uzum.uzum_final_task.runner;

import com.uzum.uzum_final_task.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountInitializationRunner implements ApplicationRunner {
    private final AccountService accountService;

    @Override
    public void run(ApplicationArguments args) {
        accountService.initializeAccounts();
    }
}
