package com.dukecash.model;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;

public class TransactionsModel extends AbstractTableModel {
    public static final int COL_DATE = 0;
    public static final int COL_DESCRIPTION = 1;
    public static final int COL_ACCOUNT = 2;
    public static final int COL_DEBIT = 3;
    public static final int COL_CREDIT = 4;
    public static final int COL_AMOUNT = 5;
    public static final int COL_PAYMENT_TYPE = 6;

    private final TreeMap<Integer, Transaction> transactions = new TreeMap<>();
    private int size;

    @Override
    public int getRowCount() {
        return size;
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case COL_DATE -> "Date";
            case COL_DESCRIPTION -> "Description";
            case COL_ACCOUNT ->  "Account";
            case COL_DEBIT -> "Debit";
            case COL_CREDIT -> "Credit";
            default -> "";
        };
    }

    @Override
    public Object getValueAt(int row, int column) {
        Entry<Integer, Transaction> entry = transactions.floorEntry(row);
        if (entry == null) {
            throw new NoSuchElementException("No transaction at row " + row);
        }

        int i = entry.getKey();
        Transaction transaction = entry.getValue();
        if (i == row) {
            return switch (column) {
                case COL_DATE -> transaction.getDate();
                case COL_DESCRIPTION -> transaction.getDescription();
                default -> null;
            };
        }

        int s = row - i - 1;
        Split split = transaction.getSplit(s);
        return switch (column) {
            case COL_DESCRIPTION -> split.getDescription();
            case COL_ACCOUNT -> split.getAccount();
            case COL_DEBIT -> split.getDebit();
            case COL_CREDIT -> split.getCredit();
            case COL_AMOUNT -> split.getAmount();
            case COL_PAYMENT_TYPE -> split.getPaymentType();
            default -> null;
        };
    }

    public boolean isTransaction(Account account, int row) {
        Entry<Integer, Transaction> entry = transactions.floorEntry(row);
        if (entry == null) {
            throw new NoSuchElementException("No transaction at row " + row);
        }
        return entry.getValue().hasAccount(account);
    }

    public void appendTransaction(LocalDate date, String description, BigDecimal amount, Account from, Account to) {
        int start = this.size;
        Transaction transaction = new Transaction(date, description, amount, from, to);
        transactions.put(start, transaction);
        this.size += 1 + transaction.getSplitCount();
        fireTableRowsInserted(start, this.size - 1);
    }

    public void deleteTransaction(int row) {
        SortedMap<Integer, Transaction> tailMap = transactions.tailMap(row);

        Iterator<Entry<Integer, Transaction>> it = tailMap.entrySet().iterator();
        if (!it.hasNext()) {
            throw new NoSuchElementException("No transaction at row " + row);
        }

        Entry<Integer, Transaction> entry = it.next();
        int start = entry.getKey();
        Transaction deleted = entry.getValue();
        it.remove();
        this.size -= 1 + deleted.getSplitCount();

        int n = start;
        for (Integer oldIndex : new ArrayList<>(tailMap.keySet())) {
            Transaction transaction = tailMap.remove(oldIndex);
            tailMap.put(n, transaction);
            n += transaction.getSplitCount();
        }

        fireTableRowsDeleted(start, start + deleted.getSplitCount());
    }
}
