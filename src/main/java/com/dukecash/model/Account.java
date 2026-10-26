package com.dukecash.model;

import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

public class Account extends DefaultMutableTreeTableNode {
    private final String name;

    public static Account sampleAccounts() {
        return new Account("Accounts",
                new Account("Assets",
                        new Account("Current",
                                new Account("Checking Account"),
                                new Account("Savings Account"))),
                new Account("Liabilities",
                        new Account("Credit Card")),
                new Account("Equity",
                        new Account("Opening Balances")),
                new Account("Income",
                        new Account("Salary")),
                new Account("Expenses",
                        new Account("Groceries"),
                        new Account("Dining Out"),
                        new Account("Entertainment"),
                        new Account("Auto",
                                new Account("Gas"),
                                new Account("Insurance"),
                                new Account("Parking"))));
    }

    public Account(String name, Account... children) {
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
}
