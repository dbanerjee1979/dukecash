package com.dukecash.model;

public enum PaymentType {
    Debit, Credit;

    public PaymentType opposite() {
        return this == Debit ? Credit : Debit;
    }
}
