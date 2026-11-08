package com.dukecash.model2;

import com.dukecash.model.Account;
import com.dukecash.model.AccountsTreeTableModel;
import com.dukecash.model.PaymentType;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionsModel extends AbstractTableModel {
    public static final int COL_DATE = 0;
    public static final int COL_DESCRIPTION = 1;
    public static final int COL_ACCOUNT = 2;
    public static final int COL_DEBIT = 3;
    public static final int COL_CREDIT = 4;

    private final List<Transaction> transactions = new ArrayList<>();

    public TransactionsModel(AccountsTreeTableModel accountsModel) {
        transactions.add(new Transaction(LocalDate.now(), "Opening Balance",
                accountsModel.findAccount("Opening Balances"),
                accountsModel.findAccount("Checking Account"),
                new BigDecimal("10000.00"),
                PaymentType.Debit));

        transactions.add(new Transaction(LocalDate.now(), "Opening Balance",
                accountsModel.findAccount("Opening Balances"),
                accountsModel.findAccount("Savings Account"),
                new BigDecimal("50000.00"),
                PaymentType.Debit));
    }

    @Override
    public int getRowCount() {
        return transactions.size();
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
            case COL_ACCOUNT -> "Transfer";
            case COL_DEBIT -> "Debit";
            case COL_CREDIT -> "Credit";
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case COL_DATE -> LocalDate.class;
            case COL_DESCRIPTION -> String.class;
            case COL_ACCOUNT -> Account.class;
            case COL_DEBIT, COL_CREDIT -> BigDecimal.class;
            default -> Object.class;
        };
    }

    @Override
    public Object getValueAt(int row, int column) {
        Transaction transaction = transactions.get(row);
        return switch (column) {
            case COL_DATE -> transaction.getDate();
            case COL_DESCRIPTION -> transaction.getDescription();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return switch (column) {
            case COL_DESCRIPTION -> true;
            default -> false;
        };
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        Transaction transaction = transactions.get(row);
        switch (column) {
            case COL_DESCRIPTION:
                transaction.setDescription((String) value);
                fireTableCellUpdated(row, column);
                break;
        }
    }

    public boolean hasAccount(int row, Account account) {
        return transactions.get(row).hasAccount(account);
    }

    public Account findTransferAccount(int row, Account account) {
        return hasAccount(row, account) ? transactions.get(row).findTransferAccount(account) : null;
    }
}
