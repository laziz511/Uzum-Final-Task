package com.uzum.uzum_final_task.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uzum.uzum_final_task.entity.Account;
import com.uzum.uzum_final_task.entity.Commission;
import com.uzum.uzum_final_task.entity.ConversionLog;
import com.uzum.uzum_final_task.exception.AccountNotFoundException;
import com.uzum.uzum_final_task.exception.CommissionNotFoundException;
import com.uzum.uzum_final_task.exception.NotEnoughMoneyException;
import com.uzum.uzum_final_task.exception.OfficialRateFetchException;
import com.uzum.uzum_final_task.model.ConversionDto;
import com.uzum.uzum_final_task.model.Currency;
import com.uzum.uzum_final_task.model.OfficialRateDto;
import com.uzum.uzum_final_task.repository.AccountRepository;
import com.uzum.uzum_final_task.repository.CommissionRepository;
import com.uzum.uzum_final_task.repository.ConversionLogRepository;
import com.uzum.uzum_final_task.service.AppService;
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
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppServiceImpl implements AppService {
    private static final String UZS = "UZS";
    private static final String DECIMAL_PATTERN = "#.######";
    private static final String API_BASE_URL = "https://cbu.uz/ru/arkhiv-kursov-valyut/json/";
    private static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat(DECIMAL_PATTERN);

    private final AccountRepository accountRepository;

    private final CommissionRepository commissionRepository;
    private final ConversionLogRepository conversionLogRepository;

    public ConversionDto getCalculation(String from, String to, Double amount) {
        Double result;
        if (UZS.equals(from) || UZS.equals(to)) {
            result = calculateCommission(getCommission(from, to), amount);
        } else {
            result = calculateCommission(getCommission(from, UZS), amount);
            result = calculateCommission(getCommission(UZS, to), result);
        }

        return new ConversionDto(from, to, DECIMAL_FORMATTER.format(result));
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
                throw new OfficialRateFetchException("Failed to fetch official exchange rate");
            }

            String rate = getRateFromResponse(response, currencies);

            return createOfficialRateModel(currencies[0], currencies[1], date, Double.parseDouble(rate));

        } catch (IOException | InterruptedException | OfficialRateFetchException e) {
            Thread.currentThread().interrupt();
            throw new OfficialRateFetchException("Failed to fetch official exchange rate");
        }
    }

    public ConversionDto convert(ConversionDto dto) {
        Account fromAccount = getAccountOrThrow(dto.getFrom());
        Account toAccount = getAccountOrThrow(dto.getTo());

        if (UZS.equals(dto.getFrom()) || UZS.equals(dto.getTo())) {

            Commission commission = getCommission(dto.getFrom(), dto.getTo());
            BigDecimal moneyToTransfer = getMoneyToTransfer(dto.getAmount());

            validateSufficientFunds(fromAccount, moneyToTransfer);

            BigDecimal moneyToSendToReceiverAccount = calculateAmountOfMoneyToSendToReceiverAccount(moneyToTransfer, commission);

            updateAccountBalances(fromAccount, toAccount, moneyToTransfer, moneyToSendToReceiverAccount);

            saveConversionLog(dto, moneyToTransfer, moneyToSendToReceiverAccount, commission);

            String convertedAmount = DECIMAL_FORMATTER.format(moneyToSendToReceiverAccount);
            return new ConversionDto(dto.getFrom(), dto.getTo(), convertedAmount);

        } else {
            Commission commission1 = getCommission(dto.getFrom(), UZS);
            Commission commission2 = getCommission(UZS, dto.getTo());

            BigDecimal moneyToTransfer = getMoneyToTransfer(dto.getAmount());

            validateSufficientFunds(fromAccount, moneyToTransfer);

            BigDecimal moneyToSendToUzsAccount = calculateAmountOfMoneyToSendToReceiverAccount(moneyToTransfer, commission1);
            BigDecimal moneyToSendToReceiverAccount = calculateAmountOfMoneyToSendToReceiverAccount(moneyToSendToUzsAccount, commission2);

            updateAccountBalances(fromAccount, toAccount, moneyToTransfer, moneyToSendToReceiverAccount);

            saveConversionLog(dto, moneyToTransfer, moneyToSendToReceiverAccount, commission1);

            String convertedAmount = DECIMAL_FORMATTER.format(moneyToSendToReceiverAccount);
            return new ConversionDto(dto.getFrom(), dto.getTo(), convertedAmount);

        }

    }

    private void saveConversionLog(ConversionDto dto, BigDecimal moneyToTransfer, BigDecimal moneyToSendToReceiverAccount, Commission commission) {
        ConversionLog conversionLog = new ConversionLog();
        conversionLog.setFromCurrency(dto.getFrom());
        conversionLog.setToCurrency(dto.getTo());
        conversionLog.setAmount(moneyToTransfer);
        conversionLog.setConvertedAmount(moneyToSendToReceiverAccount);
        conversionLog.setCommissionAmount(calculateCommissionAmount(moneyToTransfer, commission));
        conversionLog.setConversionDate(LocalDateTime.now());

        conversionLogRepository.save(conversionLog);
    }

    private BigDecimal calculateCommissionAmount(BigDecimal moneyToTransfer, Commission commission) {
        return moneyToTransfer.multiply(BigDecimal.valueOf(commission.getCommissionPercentage()));
    }

    private void updateAccountBalances(Account fromAccount, Account toAccount, BigDecimal moneyToTransfer, BigDecimal moneyToSendToReceiverAccount) {
        fromAccount.setAmount(fromAccount.getAmount().subtract(moneyToTransfer));
        toAccount.setAmount(toAccount.getAmount().add(moneyToSendToReceiverAccount));

        accountRepository.saveAll(List.of(fromAccount, toAccount));
    }

    private void validateSufficientFunds(Account fromAccount, BigDecimal moneyToTransfer) {
        if (fromAccount.getAmount().doubleValue() < moneyToTransfer.doubleValue()) {
            throw new NotEnoughMoneyException("Not enough money in account. ");
        }
    }

    private BigDecimal getMoneyToTransfer(String amount) {
        return BigDecimal.valueOf(Double.parseDouble(amount));
    }

    private Account getAccountOrThrow(String currency) {
        return accountRepository.findByCurrencyName(currency)
                .orElseThrow(() -> new AccountNotFoundException("Account not found!"));
    }

    private String getRateFromResponse(HttpResponse<String> response, String[] currencies) {
        Gson gson = new Gson();
        Type currencyListType = new TypeToken<List<Currency>>() {
        }.getType();
        List<Currency> currenciesList = gson.fromJson(response.body(), currencyListType);

        return calculateRateForCurrencyPair(currencies[0], currenciesList);
    }

    private static BigDecimal calculateAmountOfMoneyToSendToReceiverAccount(BigDecimal moneyToTransfer, Commission commission) {
        return BigDecimal.valueOf(moneyToTransfer.doubleValue() * commission.getConversionRate().doubleValue() * ((100 - commission.getCommissionPercentage()) * 0.01));
    }

    private Double calculateCommission(Commission commission, Double amount) {
        return (((100 - commission.getCommissionPercentage()) * amount) / 100) * commission.getConversionRate().doubleValue();
    }

    private String buildApiUrl(String currency, String date) {
        return API_BASE_URL + currency + "/" + date + "/";
    }

    private String calculateRateForCurrencyPair(String fromCurrency, List<Currency> currenciesList) {
        if (UZS.equals(fromCurrency)) {
            return DECIMAL_FORMATTER.format(BigDecimal.valueOf(1.0 / Double.parseDouble(currenciesList.get(0).getRate())));
        } else {
            return DECIMAL_FORMATTER.format(BigDecimal.valueOf(Double.parseDouble(currenciesList.get(0).getRate())));
        }
    }

    private OfficialRateDto createOfficialRateModel(String from, String to, String date, Double rate) {
        OfficialRateDto officialRateModel = new OfficialRateDto();
        officialRateModel.setFromCurrency(from);
        officialRateModel.setToCurrency(to);
        officialRateModel.setDate(LocalDate.parse(date));
        officialRateModel.setRate(DECIMAL_FORMATTER.format(BigDecimal.valueOf(rate)));
        return officialRateModel;
    }

    public Commission getCommission(String from, String to) {
        return commissionRepository.findByFromAndTo(from, to).
                orElseThrow(() -> new CommissionNotFoundException("Commission not found for the specified currencies"));
    }

}
