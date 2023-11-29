package com.uzum.uzum_final_task.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uzum.uzum_final_task.entity.Commission;
import com.uzum.uzum_final_task.exception.CommissionNotFoundException;
import com.uzum.uzum_final_task.exception.OfficialRateFetchException;
import com.uzum.uzum_final_task.exception.SecretKeyMismatchException;
import com.uzum.uzum_final_task.model.CommissionDto;
import com.uzum.uzum_final_task.model.ConversionDto;
import com.uzum.uzum_final_task.model.Currency;
import com.uzum.uzum_final_task.model.OfficialRateDto;
import com.uzum.uzum_final_task.repository.AccountRepository;
import com.uzum.uzum_final_task.repository.CommissionRepository;
import com.uzum.uzum_final_task.repository.SecretKeyRepository;
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
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppService {
    private static final String UZS = "UZS";
    private static final String DECIMAL_PATTERN = "#.######";
    private static final String API_BASE_URL = "https://cbu.uz/ru/arkhiv-kursov-valyut/json/";
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat(DECIMAL_PATTERN);

    private final CommissionRepository commissionRepository;
    private final SecretKeyRepository secretKeyRepository;
    private final AccountRepository accountRepository;


    public ConversionDto getCalculation(String from, String to, Double amount) {
        Double result;
        if (UZS.equals(from) || UZS.equals(to)) {
            result = calculateCommission(getCommission(from, to), amount);
        } else {
            result = calculateCommission(getCommission(from, UZS), amount);
            result = calculateCommission(getCommission(UZS, to), result);
        }

        return new ConversionDto(from, to, Double.parseDouble(DECIMAL_FORMATTER.format(result)));
    }

    public OfficialRateDto getOfficialRate(String date, String pair) {
        String[] currencies = pair.split("/");

        if (!UZS.equals(currencies[0]) && !UZS.equals(currencies[1])) {
            Commission commissionToUZS = getCommission(currencies[0], UZS);
            Commission commissionFromUZS = getCommission(UZS, currencies[1]);

            Double rateToUZS = calculateCommission(commissionToUZS, 1.0);
            Double finalRate = calculateCommission(commissionFromUZS, rateToUZS);

            return createOfficialRateModel(currencies[0], currencies[1], date, finalRate);
        }

        try {
            String apiUrl = buildApiUrl(UZS.equals(currencies[0]) ? currencies[1] : currencies[0], date);
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new OfficialRateFetchException("Failed to fetch official exchange rate. HTTP Status Code: " + response.statusCode());
            }

            Gson gson = new Gson();
            Type currencyListType = new TypeToken<List<Currency>>() {
            }.getType();
            List<Currency> currenciesList = gson.fromJson(response.body(), currencyListType);

            String rate = calculateRateForCurrencyPair(currencies[0], currencies[1], currenciesList);

            return createOfficialRateModel(currencies[0], currencies[1], date, Double.parseDouble(rate));

        } catch (IOException | InterruptedException | OfficialRateFetchException e) {
            Thread.currentThread().interrupt();
            throw new OfficialRateFetchException("Failed to fetch official exchange rate.");
        }
    }

    public CommissionDto setCommission(String secretKey, CommissionDto commissionModel) {
        if (!validateSecretKey(secretKey)) {
            throw new SecretKeyMismatchException("Secret key mismatch. Access denied.");
        }

        Commission existingCommission = getCommission(commissionModel.getFrom(), commissionModel.getTo());
        existingCommission.setCommissionPercentage(commissionModel.getCommissionPercentage());
        commissionRepository.save(existingCommission);

        return commissionModel;
    }

    public ConversionDto convert(ConversionDto dto) {
        return null;
    }


    private Commission getCommission(String from, String to) {
        return commissionRepository.findByFromAndTo(from, to).orElseThrow(() -> new CommissionNotFoundException("Commission not found for the specified currencies"));
    }

    private Double calculateCommission(Commission commission, Double amount) {
        return (((100 - commission.getCommissionPercentage()) * amount) / 100) * commission.getConversionRate();
    }

    private String buildApiUrl(String currency, String date) {
        return API_BASE_URL + currency + "/" + date + "/";
    }

    private String calculateRateForCurrencyPair(String fromCurrency, String toCurrency, List<Currency> currenciesList) {
        if (UZS.equals(fromCurrency)) {
            return DECIMAL_FORMATTER.format(BigDecimal.valueOf(1.0 / Double.parseDouble(currenciesList.get(0).getRate())));
        } else {
            return DECIMAL_FORMATTER.format(BigDecimal.valueOf(Double.parseDouble(currenciesList.get(0).getRate())));
        }
    }

    private boolean validateSecretKey(String inputKey) {
        return secretKeyRepository.findByKeyValue(inputKey).isPresent();
    }

    private OfficialRateDto createOfficialRateModel(String from, String to, String date, Double rate) {
        OfficialRateDto officialRateModel = new OfficialRateDto();
        officialRateModel.setFromCurrency(from);
        officialRateModel.setToCurrency(to);
        officialRateModel.setDate(LocalDate.parse(date));
        officialRateModel.setRate(DECIMAL_FORMATTER.format(BigDecimal.valueOf(rate)));
        return officialRateModel;
    }

}
