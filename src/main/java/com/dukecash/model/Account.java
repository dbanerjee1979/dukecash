package com.dukecash.model;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

public class Account extends DefaultMutableTreeTableNode {
    private final String name;
    private final boolean placeholder;

    public static Account sampleAccounts() {
        return new Account("Accounts", true,
                new Account("Assets", true,
                        new Account("Current", true,
                                new Account("Checking Account", false),
                                new Account("Savings Account", false))),
                new Account("Liabilities", true,
                        new Account("Credit Card", false)),
                new Account("Equity", true,
                        new Account("Opening Balances", false)),
                new Account("Income", true,
                        new Account("Salary", false)),
                new Account("Expenses", true,
                        new Account("Groceries", false),
                        new Account("Dining Out", false),
                        new Account("Entertainment", false),
                        new Account("Auto", true,
                                new Account("Gas", false),
                                new Account("Insurance", false),
                                new Account("Parking", false))));
    }

    public Account(String name, boolean placeholder, Account... children) {
        this.placeholder = placeholder;
        setUserObject(this);
        this.name = name;
        for (Account child : children) {
            add(child);
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
}
