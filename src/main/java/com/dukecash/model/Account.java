package com.dukecash.model;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

import javax.swing.event.TableModelEvent;
import javax.swing.tree.TreePath;
import java.math.BigDecimal;
import java.util.*;

public class Account extends DefaultMutableTreeTableNode {
    private final String name;
    private final boolean placeholder;
    private final PaymentType accountType;
    private AccountTransactionModel accountTableModel;
    private BigDecimal balance = BigDecimal.ZERO;
    private AccountsTreeTableModel accountsTreeTableModel;

    public static Account sampleAccounts(TransactionsModel transactionsModel) {
        return new Account("Accounts", true, transactionsModel, null,
                new Account("Assets", true, transactionsModel, PaymentType.Debit,
                        new Account("Current", true, transactionsModel, PaymentType.Debit,
                                new Account("Checking Account", false, transactionsModel, PaymentType.Debit),
                                new Account("Savings Account", false, transactionsModel, PaymentType.Debit))),
                new Account("Liabilities", true, transactionsModel, PaymentType.Credit,
                        new Account("Credit Card", false, transactionsModel, PaymentType.Credit)),
                new Account("Equity", true, transactionsModel, PaymentType.Credit,
                        new Account("Opening Balances", false, transactionsModel, PaymentType.Credit)),
                new Account("Income", true, transactionsModel, PaymentType.Credit,
                        new Account("Salary", false, transactionsModel, PaymentType.Credit)),
                new Account("Expenses", true, transactionsModel, PaymentType.Debit,
                        new Account("Groceries", false, transactionsModel, PaymentType.Debit),
                        new Account("Dining Out", false, transactionsModel, PaymentType.Debit),
                        new Account("Entertainment", false, transactionsModel, PaymentType.Debit),
                        new Account("Auto", true, transactionsModel, PaymentType.Debit,
                                new Account("Gas", false, transactionsModel, PaymentType.Debit),
                                new Account("Insurance", false, transactionsModel, PaymentType.Debit),
                                new Account("Parking", false, transactionsModel, PaymentType.Debit))));
    }

    public Account(String name, boolean placeholder, TransactionsModel transactionsModel, PaymentType accountType, Account... children) {
        this.placeholder = placeholder;
        this.accountType = accountType;
        setUserObject(this);
        this.name = name;
        for (Account child : children) {
            add(child);
        }

        if (!this.placeholder) {
            this.accountTableModel = new AccountTransactionModel(this, transactionsModel);
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

        if (placeholder) {
            for (MutableTreeTableNode child : children) {
                balance = balance.add(((Account) child).getBalance());
            }
        } else {
            for (int row = 0; row < accountTableModel.getRowCount(); row++) {
                BigDecimal amount = (BigDecimal) accountTableModel.getValueAt(row, AccountTransactionModel.COL_AMOUNT);
                PaymentType paymentType = (PaymentType) accountTableModel.getValueAt(row, AccountTransactionModel.COL_PAYMENT_TYPE);
                if (accountType == PaymentType.Debit) {
                    if (paymentType == PaymentType.Debit) {
                        balance = balance.add(amount);
                    } else {
                        balance = balance.subtract(amount);
                    }
                } else {
                    if (paymentType == PaymentType.Credit) {
                        balance = balance.add(amount);
                    } else {
                        balance = balance.subtract(amount);
                    }
                }
            }
        }

        if (!this.balance.equals(balance)) {
            this.balance = balance;
            accountsTreeTableModel.fireAccountBalanceChanged(this);
            TreeTableNode parent = getParent();
            if (parent != null) {
                ((Account) parent).updateBalance(ev);
            }
        }
    }

    public Set<Account> findAllAccounts() {
        if (getChildCount() == 0) {
            return Set.of(this);
        }
        TreeSet<Account> accounts = new TreeSet<>(Comparator.comparing(Account::getName));
        for (int i = 0; i < getChildCount(); i++) {
            accounts.addAll(((Account) getChildAt(i)).findAllAccounts());
        }
        return accounts;
    }
}
