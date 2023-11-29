package com.uzum.uzum_final_task.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfficialRateDto {
    private String fromCurrency;
    private String toCurrency;
    private LocalDate date;
    private String rate;
}
