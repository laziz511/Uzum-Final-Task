package com.uzum.uzum_final_task.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uzum.uzum_final_task.entity.Commission;
import com.uzum.uzum_final_task.exception.CommissionNotFoundException;
import com.uzum.uzum_final_task.exception.SecretKeyMismatchException;
import com.uzum.uzum_final_task.model.CommissionDto;
import com.uzum.uzum_final_task.model.Currency;
import com.uzum.uzum_final_task.repository.CommissionRepository;
import com.uzum.uzum_final_task.repository.SecretKeyRepository;
import com.uzum.uzum_final_task.service.CommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommissionServiceImpl implements CommissionService {
    private static final String UZS = "UZS";
    private static final double DEFAULT_COMMISSION_PERCENTAGE = 0.0;
    private static final String API_BASE_URL = "https://cbu.uz/ru/arkhiv-kursov-valyut/json/all/";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final CommissionRepository commissionRepository;
    private final SecretKeyRepository secretKeyRepository;

    public CommissionDto setCommission(String secretKey, CommissionDto commissionModel) {
        if (!validateSecretKey(secretKey)) {
            throw new SecretKeyMismatchException("Secret key mismatch. Access denied.");
        }

        Commission existingCommission = getCommission(commissionModel.getFrom(), commissionModel.getTo());
        existingCommission.setCommissionPercentage(commissionModel.getCommissionPercentage());
        commissionRepository.save(existingCommission);

        return commissionModel;
    }


    public void updateCommissions() {
        List<Currency> currencies = fetchCurrencies();

        if (currencies.isEmpty()) {
            log.warn("No currency data found in the response.");
            return;
        }

        saveCommissions(currencies);
    }

    public List<Currency> fetchCurrencies() {
        try {
            String apiUrl = buildApiUrl();
            log.info("Fetching exchange rates from: {}", apiUrl);

            HttpResponse<String> response = sendHttpRequest(apiUrl);

            handleHttpResponse(response);

            return handleResponse(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Failed to fetch exchange rate.", e);
        }
        return Collections.emptyList();
    }

    private Commission getCommission(String from, String to) {
        return commissionRepository.findByFromAndTo(from, to)
                .orElseThrow(() -> new CommissionNotFoundException("Commission not found for the specified currencies"));
    }

    private String buildApiUrl() {
        return API_BASE_URL + LocalDate.now() + "/";
    }

    private HttpResponse<String> sendHttpRequest(String apiUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<Currency> handleResponse(String responseBody) {
        Gson gson = new Gson();
        Type currencyListType = new TypeToken<List<Currency>>() {
        }.getType();
        return gson.fromJson(responseBody, currencyListType);
    }

    private void saveCommissions(List<Currency> currencies) {
        currencies.forEach(currency -> {
            BigDecimal rate = new BigDecimal(currency.getRate());
            saveCommission(UZS, currency.getCcy(), DEFAULT_COMMISSION_PERCENTAGE, BigDecimal.valueOf(1.0 / rate.doubleValue()));
            saveCommission(currency.getCcy(), UZS, DEFAULT_COMMISSION_PERCENTAGE, rate);
        });
    }

    private void saveCommission(String from, String to, double commissionPercentage, BigDecimal conversionRate) {
        Optional<Commission> existingCommission = commissionRepository.findByFromAndTo(from, to);

        Commission commission;
        if (existingCommission.isPresent()) {
            commission = existingCommission.get();
            commission.setConversionRate(conversionRate);
        } else {
            commission = new Commission(from, to, commissionPercentage, conversionRate);
        }
        commissionRepository.save(commission);
    }

    private boolean validateSecretKey(String inputKey) {
        return secretKeyRepository.findByKeyValue(inputKey).isPresent();
    }


    private void handleHttpResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            log.warn("Failed to fetch exchange rate. HTTP Status Code: " + response.statusCode());
        }
    }
}
