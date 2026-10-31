package com.dukecash.model;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Account extends DefaultMutableTreeTableNode {
    private final String name;
    private final boolean placeholder;
    private FilterTableModel accountTableModel;
    private BigDecimal balance = BigDecimal.ZERO;
    private AccountsTreeTableModel accountsTreeTableModel;

    public static Account sampleAccounts(TransactionsModel transactionsModel) {
        return new Account("Accounts", true,
                transactionsModel, new Account("Assets", true,
                transactionsModel, new Account("Current", true,
                transactionsModel, new Account("Checking Account", false, transactionsModel),
                                new Account("Savings Account", false, transactionsModel))),
                new Account("Liabilities", true,
                        transactionsModel, new Account("Credit Card", false, transactionsModel)),
                new Account("Equity", true,
                        transactionsModel, new Account("Opening Balances", false, transactionsModel)),
                new Account("Income", true,
                        transactionsModel, new Account("Salary", false, transactionsModel)),
                new Account("Expenses", true,
                        transactionsModel, new Account("Groceries", false, transactionsModel),
                        new Account("Dining Out", false, transactionsModel),
                        new Account("Entertainment", false, transactionsModel),
                        new Account("Auto", true,
                                transactionsModel, new Account("Gas", false, transactionsModel),
                                new Account("Insurance", false, transactionsModel),
                                new Account("Parking", false, transactionsModel))));
    }

    public Account(String name, boolean placeholder, TransactionsModel transactionsModel, Account... children) {
        this.placeholder = placeholder;
        setUserObject(this);
        this.name = name;
        for (Account child : children) {
            add(child);
        }

        if (!this.placeholder) {
            this.accountTableModel = new FilterTableModel(transactionsModel,
                    row -> transactionsModel.isTransaction(Account.this, row));
            this.accountTableModel.addTableModelListener(this::updateBalance);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public Account findAccount(String name) {
        Account found = findAccountImpl(name);
        if (found == null) {
            throw new NoSuchElementException("Account with name %s not found".formatted(name));
        }
        return found;
    }

    private Account findAccountImpl(String name) {
        if (this.name.equals(name)) {
            return this;
        }
        for (MutableTreeTableNode child : children) {
            Account account = (Account) child;
            Account found = account.findAccountImpl(name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public TableModel getTableModel() {
        if (placeholder) {
            throw new IllegalStateException("Attempted to get row sorter on placeholder account");
        }
        return accountTableModel;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setAccountsTreeTableModel(AccountsTreeTableModel accountsTreeTableModel) {
        this.accountsTreeTableModel = accountsTreeTableModel;
        for (MutableTreeTableNode child : children) {
            ((Account) child).setAccountsTreeTableModel(accountsTreeTableModel);
        }
    }

    public TreePath getPath() {
        List<Account> path = new ArrayList<>();
        for (Account account = this; account != null; account = (Account) account.getParent()) {
            path.addFirst(account);
        }
        return new TreePath(path.toArray(Object[]::new));
    }

    private void updateBalance(TableModelEvent ev) {
        BigDecimal balance = BigDecimal.ZERO;
        for (int row = 0; row < accountTableModel.getRowCount(); row++) {
            if (accountTableModel.getValueAt(row, TransactionsModel.COL_ACCOUNT) == Account.this) {
                balance = balance.add((BigDecimal) accountTableModel.getValueAt(row, TransactionsModel.COL_AMOUNT));
            }
        }
        if (!this.balance.equals(balance)) {
            this.balance = balance;
            accountsTreeTableModel.fireAccountBalanceChanged(this);
        }
    }
}
