package com.uzum.uzum_final_task.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "commissions")
public class Commission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_currency", nullable = false, length = 3)
    private String from;

    @Column(name = "to_currency", nullable = false, length = 3)
    private String to;

    @Column(name = "commission_percentage", nullable = false)
    private Double commissionPercentage;

    @Column(name = "conversion_rate", nullable = false, precision = 38, scale = 20)
    private BigDecimal conversionRate;

    public Commission(String from, String to, Double commissionPercentage, BigDecimal conversionRate) {
        this.from = from;
        this.to = to;
        this.commissionPercentage = commissionPercentage;
        this.conversionRate = conversionRate;
    }
}