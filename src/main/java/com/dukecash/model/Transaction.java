package com.dukecash.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private final LocalDate date;
    private String description;
    private final List<Split> splits = new ArrayList<>();

    public Transaction(LocalDate date, String description, Account from, Account to, BigDecimal amount,
                       PaymentType paymentType) {
        this.date = date;
        this.description = description;
        this.splits.add(new Split(this, description, to, amount, paymentType));
        this.splits.add(new Split(this, description, from, amount, paymentType.opposite()));
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

    public Account findTransferAccount(Account from) {
        if (splits.size() == 2) {
            for (Split split : splits) {
                if (split.getAccount() != from) {
                    return split.getAccount();
                }
            }
        }
        return null;
    }

    public void updateAccount(Account fromAccount, Account toAccount) {
        if (splits.size() == 2) {
            for (Split split : splits) {
                if (split.getAccount() != fromAccount) {
                    split.setAccount(toAccount);
                    return;
                }
            }
        }
    }

    public BigDecimal findAmountForAccount(Account account) {
        return findAccount(account).getAmount();
    }

    public PaymentType findPaymentTypeForAccount(Account account) {
        return findAccount(account).getPaymentType();
    }

    private Split findAccount(Account account) {
        return splits.stream()
                .filter(split -> split.getAccount() == account)
                .findFirst()
                .orElseThrow();
    }
}
