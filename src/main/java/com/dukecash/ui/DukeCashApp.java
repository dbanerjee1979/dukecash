package com.dukecash.ui;

import com.dukecash.model.Account;
import com.dukecash.model.AccountsTreeTableModel;
import com.dukecash.model.TransactionsModel;

import javax.swing.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class DukeCashApp extends JFrame {

    public DukeCashApp() {
        super("DukeCash");

        ListSelectionModel accountSelection = new DefaultListSelectionModel();
        accountSelection.setSelectionMode(SINGLE_SELECTION);

        DefaultListModel<Account> openAccounts = new DefaultListModel<>();

        AccountsTreeTableModel accountsModel = new AccountsTreeTableModel();

        TransactionsModel transactionsModel = new TransactionsModel();
        transactionsModel.appendTransaction(LocalDate.now(), "Opening Balance", new BigDecimal("100000"),
                accountsModel.findAccount("Opening Balances"),
                accountsModel.findAccount("Checking Account"));
        transactionsModel.appendTransaction(LocalDate.now(), "Opening Balance", new BigDecimal("500000"),
                accountsModel.findAccount("Opening Balances"),
                accountsModel.findAccount("Savings Account"));
        transactionsModel.deleteTransaction(0);

        add(new AccountTabs(accountsModel, openAccounts, accountSelection), CENTER);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(new WindowMenu(openAccounts, accountSelection));
        add(menuBar, NORTH);

        setSize(1024, 768);
    }

    static void main() {
        System.setProperty("swing.metalTheme", "steel");
        SwingUtilities.invokeLater(DukeCashApp::run);
    }

    private static void run() {
        DukeCashApp app = new DukeCashApp();
        app.setDefaultCloseOperation(EXIT_ON_CLOSE);
        app.setVisible(true);
    }
}
