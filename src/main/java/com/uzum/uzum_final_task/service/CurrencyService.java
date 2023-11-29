package com.uzum.uzum_final_task.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uzum.uzum_final_task.entity.Commission;
import com.uzum.uzum_final_task.exception.ExchangeRateFetchException;
import com.uzum.uzum_final_task.exception.NoCurrencyDataException;
import com.uzum.uzum_final_task.model.Currency;
import com.uzum.uzum_final_task.repository.CommissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {
    private static final String UZS = "UZS";
    private static final double DEFAULT_COMMISSION_PERCENTAGE = 0.0;
    private static final String API_BASE_URL = "https://cbu.uz/ru/arkhiv-kursov-valyut/json/all/";

    private final CommissionRepository commissionRepository;

    public void updateCurrency() {
        List<Currency> currencies = fetchCurrencies();

        if (currencies.isEmpty()) {
            throw new NoCurrencyDataException("No currency data found in the response.");
        }

        saveCommissions(currencies);
    }

    private String buildApiUrl() {
        return API_BASE_URL + LocalDate.now() + "/";
    }

    private List<Currency> parseResponse(String responseBody) {
        Gson gson = new Gson();
        Type currencyListType = new TypeToken<List<Currency>>() {
        }.getType();
        return gson.fromJson(responseBody, currencyListType);
    }

    private void saveCommissions(List<Currency> currencies) {
        currencies.forEach(currency -> {
            saveCommission(UZS, currency.getCcy(), DEFAULT_COMMISSION_PERCENTAGE, 1.0 / Double.parseDouble(currency.getRate()));
            saveCommission(currency.getCcy(), UZS, DEFAULT_COMMISSION_PERCENTAGE, Double.parseDouble(currency.getRate()));
        });
    }

    private void saveCommission(String from, String to, double commissionPercentage, double conversionRate) {
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

    public List<Currency> fetchCurrencies() {
        try {
            String apiUrl = buildApiUrl();
            log.info("Fetching exchange rates from: {}", apiUrl);

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ExchangeRateFetchException("Failed to fetch exchange rate. HTTP Status Code: " + response.statusCode());
            }

            return parseResponse(response.body());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExchangeRateFetchException("Failed to fetch exchange rate.");
        }
    }

}
