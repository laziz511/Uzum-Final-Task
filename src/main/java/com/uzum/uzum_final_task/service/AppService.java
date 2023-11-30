package com.uzum.uzum_final_task.service;

import com.uzum.uzum_final_task.model.ConversionDto;
import com.uzum.uzum_final_task.model.OfficialRateDto;

public interface AppService {
    ConversionDto getCalculation(String from, String to, Double amount);
    OfficialRateDto getOfficialRate(String date, String pair);
    ConversionDto convert(ConversionDto dto);
}
