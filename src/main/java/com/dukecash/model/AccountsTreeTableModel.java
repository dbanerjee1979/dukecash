package com.dukecash.model;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import java.math.BigDecimal;
import java.util.Set;

public class AccountsTreeTableModel extends AbstractTreeTableModel {
    public static final int COL_NAME = 0;
    public static final int COL_BALANCE = 1;

    public AccountsTreeTableModel(TransactionsModel transactionsModel) {
        super(Account.sampleAccounts(transactionsModel));
        ((Account) getRoot()).setAccountsTreeTableModel(this);
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case COL_NAME -> "Name";
            case COL_BALANCE -> "Balance";
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case COL_NAME -> String.class;
            case COL_BALANCE -> BigDecimal.class;
            default -> Object.class;
        };
    }

    @Override
    public Object getValueAt(Object row, int column) {
        Account account = (Account) row;
        return switch (column) {
            case COL_NAME -> account.getName();
            case COL_BALANCE -> account.getBalance();
            default -> null;
        };
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((Account) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((Account) parent).getChildCount();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((Account) parent).getIndex((Account) child);
    }

    public Account findAccount(String name) {
        return ((Account) root).findAccount(name);
    }

    public void fireAccountBalanceChanged(Account account) {
        modelSupport.firePathChanged(account.getPath());
    }

    public Set<Account> findAllAccounts() {
        return ((Account) getRoot()).findAllAccounts();
    }
}
