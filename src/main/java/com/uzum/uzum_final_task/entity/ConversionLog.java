package com.uzum.uzum_final_task.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conversion_logs")
public class ConversionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_currency", nullable = false)
    private String fromCurrency;

    @Column(name = "to_currency", nullable = false)
    private String toCurrency;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "converted_amount", nullable = false)
    private Double convertedAmount;

    @Column(name = "commission_amount", nullable = false)
    private Double commissionAmount;

    @Column(name = "conversion_date", nullable = false)
    private LocalDateTime conversionDate;

}
