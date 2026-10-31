package com.dukecash.model;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.TreePath;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Account extends DefaultMutableTreeTableNode {
    private final String name;
    private final boolean placeholder;
    private TableRowSorter<TransactionsModel> rowSorter;
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
            this.rowSorter = new TableRowSorter<>(transactionsModel);
            this.rowSorter.setRowFilter(new AccountRowFilter());
            transactionsModel.addTableModelListener(new SortUpdatingTableModelListener());
            this.rowSorter.addRowSorterListener(new TransactionRowSortListener(transactionsModel));
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

    public RowSorter<? extends TableModel> getRowSorter() {
        if (placeholder) {
            throw new IllegalStateException("Attempted to get row sorter on placeholder account");
        }
        return rowSorter;
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

    private class SortUpdatingTableModelListener implements TableModelListener {
        boolean ignoreSortChange;

        @Override
        public void tableChanged(TableModelEvent ev) {
            try {
                ignoreSortChange = true;
                switch(ev.getType()) {
                    case TableModelEvent.UPDATE:
                        if (ev.getLastRow() == Integer.MAX_VALUE) {
                            rowSorter.allRowsChanged();
                        } else if (ev.getColumn() == TableModelEvent.ALL_COLUMNS) {
                            rowSorter.rowsUpdated(ev.getFirstRow(), ev.getLastRow());
                        } else {
                            rowSorter.rowsUpdated(ev.getFirstRow(), ev.getLastRow(), ev.getColumn());
                        }
                        break;
                    case TableModelEvent.INSERT:
                        rowSorter.rowsInserted(ev.getFirstRow(), ev.getLastRow());
                        break;
                    case TableModelEvent.DELETE:
                        rowSorter.rowsDeleted(ev.getFirstRow(), ev.getLastRow());
                        break;
                }
            } finally {
                ignoreSortChange = false;
            }
        }
    }

    private class AccountRowFilter extends RowFilter<TransactionsModel, Integer> {
        @Override
        public boolean include(Entry<? extends TransactionsModel, ? extends Integer> entry) {
            return entry.getModel().isTransaction(Account.this, entry.getIdentifier());
        }
    }

    private class TransactionRowSortListener implements RowSorterListener {
        private final TransactionsModel transactionsModel;

        public TransactionRowSortListener(TransactionsModel transactionsModel) {
            this.transactionsModel = transactionsModel;
        }

        @Override
        public void sorterChanged(RowSorterEvent ev) {
            BigDecimal balance = BigDecimal.ZERO;
            for (int view = 0; view < Account.this.rowSorter.getViewRowCount(); view++) {
                int row = Account.this.rowSorter.convertRowIndexToModel(view);
                if (transactionsModel.getValueAt(row, TransactionsModel.COL_ACCOUNT) == Account.this) {
                    balance = balance.add((BigDecimal) transactionsModel.getValueAt(row, TransactionsModel.COL_AMOUNT));
                }
            }
            if (!Account.this.balance.equals(balance)) {
                Account.this.balance = balance;
                Account.this.accountsTreeTableModel.fireAccountBalanceChanged(Account.this);
            }
        }
    }
}
