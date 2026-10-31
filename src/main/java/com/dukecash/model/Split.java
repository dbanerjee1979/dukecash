package com.dukecash.model;

import java.math.BigDecimal;

public class Split {
    private final Transaction transaction;
    private final String description;
    private final Account account;
    private final BigDecimal amount;
    private final PaymentType paymentType;

    public Split(Transaction transaction, String description, Account account, BigDecimal amount, PaymentType paymentType) {
        this.transaction = transaction;
        this.description = description;
        this.account = account;
        this.amount = amount;
        this.paymentType = paymentType;
    }

    public String getDescription() {
        return description;
    }

    public Account getAccount() {
        return account;
    }

    public BigDecimal getDebit() {
        return paymentType == PaymentType.Debit ? amount : null;
    }

    public BigDecimal getCredit() {
        return paymentType == PaymentType.Credit ? amount : null;
    }
}
