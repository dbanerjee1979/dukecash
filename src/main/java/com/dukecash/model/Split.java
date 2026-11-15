package com.dukecash.model;

import java.math.BigDecimal;

public class Split {
    private final Transaction transaction;
    private final String description;
    private Account account;
    private final BigDecimal amount;
    private final PaymentType paymentType;

    public Split(Transaction transaction, String description, Account account, BigDecimal amount, PaymentType paymentType) {
        this.transaction = transaction;
        this.description = description;
        this.account = account;
        this.amount = amount;
        this.paymentType = paymentType;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }
}
