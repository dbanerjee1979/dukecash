package com.dukecash.model;

import com.dukecash.model.util.FilteringTableModel;

import java.math.BigDecimal;

import static com.dukecash.model.TransactionsModel.COL_ACCOUNT;

public class AccountTransactionModel extends FilteringTableModel {
    private static final int COL_BALANCE = 5;
    public static final int COL_AMOUNT = 6;
    public static final int COL_PAYMENT_TYPE = 7;

    private final Account account;
    private final TransactionsModel transactionsModel;

    public AccountTransactionModel(Account account, TransactionsModel transactionsModel) {
        super(transactionsModel, sourceRow -> transactionsModel.hasAccount(sourceRow, account));
        this.account = account;
        this.transactionsModel = transactionsModel;
    }

    @Override
    public int getColumnCount() {
        return transactionsModel.getColumnCount() + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column < super.getColumnCount()) {
            return super.getColumnName(column);
        }
        else if (column == COL_BALANCE) {
            return "Balance";
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column < super.getColumnCount()) {
            return source.getColumnClass(column);
        }
        else if (column == COL_BALANCE) {
            return BigDecimal.class;
        }
        return Object.class;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Transaction transaction = (Transaction) super.getValueAt(row, TransactionsModel.COL_TRANSACTION);
        Account transfer = transaction.findTransferAccount(account);
        return switch (column) {
            case COL_ACCOUNT -> transfer;
            case COL_BALANCE -> BigDecimal.ZERO;
            case COL_AMOUNT -> transaction.findAmountForAccount(account);
            case COL_PAYMENT_TYPE -> transaction.findPaymentTypeForAccount(account);
            default ->  super.getValueAt(row, column);
        };
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == COL_ACCOUNT) {
            return true;
        }
        return super.isCellEditable(row, column);
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        int sourceRow = getSourceRowForViewRow(row);
        if (column == COL_ACCOUNT) {
            transactionsModel.updateToAccount(sourceRow, account, (Account) value);
        } else {
            transactionsModel.setValueAt(value, sourceRow, column);
        }
    }
}
