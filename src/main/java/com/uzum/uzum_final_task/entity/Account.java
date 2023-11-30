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
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency_name", nullable = false, length = 3)
    private String currencyName;

    @Column(name = "amount", nullable = false, precision = 38, scale = 6)
    private BigDecimal amount;
}