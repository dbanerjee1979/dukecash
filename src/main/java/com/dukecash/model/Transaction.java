package com.dukecash.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private LocalDate date;
    private String description;
    private List<Split> splits = new ArrayList<>();

    public Transaction(LocalDate date, String description, BigDecimal amount, Account from, Account to) {
        this.date = date;
        this.description = description;
        this.splits.add(new Split(this, description, from, amount, PaymentType.Credit));
        this.splits.add(new Split(this, description, to, amount, PaymentType.Debit));
    }

    public Split getSplit(int s) {
        return splits.get(s);
    }

    public int getSplitCount() {
        return splits.size();
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean hasAccount(Account account) {
        return splits.stream().anyMatch(split -> split.getAccount() == account);
    }

    public void updateSplitAmount(int splitIndex, BigDecimal amount, PaymentType paymentType) {
        Split split = splits.get(splitIndex);
        splits.set(splitIndex, new Split(this, split.getDescription(), split.getAccount(), amount, paymentType));
        int otherIndex = 1 - splitIndex;
        Split other = splits.get(otherIndex);
        splits.set(otherIndex, new Split(this, other.getDescription(), other.getAccount(), amount, paymentType.opposite()));
    }

    public void updateSplitDescription(int splitIndex, String description) {
        Split split = splits.get(splitIndex);
        splits.set(splitIndex, new Split(this, description, split.getAccount(), split.getAmount(), split.getPaymentType()));
    }
}
