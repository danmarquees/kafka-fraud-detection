package com.example.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.math.BigDecimal;

@Entity
public class FraudulentTransaction {

    @Id
    private String transactionId;

    private String userId;
    private BigDecimal amount;
    private long timestamp;

    // Getters, Setters e Construtores
}
