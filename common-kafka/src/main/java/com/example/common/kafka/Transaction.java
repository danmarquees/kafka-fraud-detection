package com.example.common.kafka;

import java.math.BigDecimal;

public class Transaction {

    private String transactionId;
    private String userId;
    private BigDecimal amount;
    private long timestamp;

    // Construtores, getters, setters e toString()
    public Transaction() {}

    public Transaction(
        String transactionId,
        String userId,
        BigDecimal amount,
        long timestamp
    ) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return (
            "Transaction{" +
            "transactionId='" +
            transactionId +
            '\'' +
            ", userId='" +
            userId +
            '\'' +
            ", amount=" +
            amount +
            ", timestamp=" +
            timestamp +
            '}'
        );
    }
}
