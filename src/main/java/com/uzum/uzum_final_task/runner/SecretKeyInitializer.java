package com.uzum.uzum_final_task.runner;

import com.uzum.uzum_final_task.service.SecretKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecretKeyInitializer implements ApplicationRunner {
    private final SecretKeyService secretKeyService;

    @Override
    public void run(ApplicationArguments args) {
        secretKeyService.initializeSecretKey();
    }
}