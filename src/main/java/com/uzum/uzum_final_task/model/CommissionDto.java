package com.uzum.uzum_final_task.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommissionDto {
    private String from;
    private String to;
    private Double commissionPercentage;
}
