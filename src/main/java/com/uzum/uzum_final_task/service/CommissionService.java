package com.uzum.uzum_final_task.service;

import com.uzum.uzum_final_task.model.CommissionDto;
import com.uzum.uzum_final_task.model.Currency;

import java.util.List;

public interface CommissionService {
    void updateCommissions();

    List<Currency> fetchCurrencies();

    CommissionDto setCommission(String secretKey, CommissionDto commissionModel);
}
