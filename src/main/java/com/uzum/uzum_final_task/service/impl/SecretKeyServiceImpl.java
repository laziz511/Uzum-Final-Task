package com.uzum.uzum_final_task.service.impl;

import com.uzum.uzum_final_task.entity.SecretKey;
import com.uzum.uzum_final_task.repository.SecretKeyRepository;
import com.uzum.uzum_final_task.service.SecretKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecretKeyServiceImpl implements SecretKeyService {
    private final SecretKeyRepository secretKeyRepository;

    @Value("${secret.key.file.path}")
    private String secretKeyFilePath;

    public void initializeSecretKey() {
        try {
            String secretKeyValue = readSecretKeyFromFile();
            saveSecretKeyToDatabase(secretKeyValue);
        } catch (IOException e) {
            log.warn("Failed to read the secret key file.", e);
        }
    }

    private String readSecretKeyFromFile() throws IOException {
        ClassPathResource resource = new ClassPathResource(secretKeyFilePath);
        return new String(resource.getInputStream().readAllBytes());
    }

    private void saveSecretKeyToDatabase(String secretKeyValue) {
        if (secretKeyRepository.findByKeyValue(secretKeyValue).isEmpty()) {
            SecretKey secretKey = new SecretKey();
            secretKey.setKeyValue(secretKeyValue);
            secretKeyRepository.save(secretKey);
        }
    }
}
