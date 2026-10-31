package com.dukecash.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionItem {
    LocalDate getDate();
    String getDescription();
    Account getAccount();
    BigDecimal getDebit();
    BigDecimal getCredit();
}
