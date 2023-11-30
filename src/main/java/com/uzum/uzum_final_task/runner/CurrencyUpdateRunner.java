package com.uzum.uzum_final_task.runner;

import com.uzum.uzum_final_task.service.CommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrencyUpdateRunner implements ApplicationRunner {
    private final CommissionService currencyService;

    @Override
    public void run(ApplicationArguments args) {
        currencyService.updateCommissions();
    }
}
