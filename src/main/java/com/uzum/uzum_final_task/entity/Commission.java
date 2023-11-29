package com.uzum.uzum_final_task.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "from_currency", nullable = false, length = 10)
    private String from;

    @Column(name = "to_currency", nullable = false, length = 10)
    private String to;

    @Column(name = "commission_percentage", nullable = false)
    private Double commissionPercentage;

    @Column(name = "conversion_rate", nullable = false)
    private Double conversionRate;

    public Commission(String from, String to, Double commissionPercentage, Double conversionRate) {
        this.from = from;
        this.to = to;
        this.commissionPercentage = commissionPercentage;
        this.conversionRate = conversionRate;
    }
}