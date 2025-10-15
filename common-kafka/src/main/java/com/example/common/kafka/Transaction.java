package com.example.common.kafka;

import java.math.BigDecimal;

public class Transaction {

    private String transactionID;
    private String userID;
    private BigDecimal amount;
    private long timestamp;

    //Construttores, getters, setters e toString()
    public Transaction() {}

    public Transaction(
        String transactionID,
        String userID,
        BigDecimal amount,
        long timestamp
    ) {
        this.transactionID = transactionID;
        this.userID = userID;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionID;
    }

    public String getUserID() {
        return userID;
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
            "transactionID='" +
            transactionID +
            '\'' +
            ", userID='" +
            userID +
            '\'' +
            ", amount=" +
            amount +
            ", timestamp=" +
            timestamp +
        '}';
    }
}
