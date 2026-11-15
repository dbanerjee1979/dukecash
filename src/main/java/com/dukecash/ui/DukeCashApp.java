package com.dukecash.ui;

import com.dukecash.model.Account;
import com.dukecash.model.AccountsTreeTableModel;
import com.dukecash.model.TransactionsModel;

import javax.swing.*;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class DukeCashApp extends JFrame {

    public DukeCashApp() {
        super("DukeCash");

        ListSelectionModel accountSelection = new DefaultListSelectionModel();
        accountSelection.setSelectionMode(SINGLE_SELECTION);

        DefaultListModel<Account> openAccounts = new DefaultListModel<>();

        TransactionsModel transactionsModel = new TransactionsModel();
        AccountsTreeTableModel accountsModel = new AccountsTreeTableModel(transactionsModel);
        transactionsModel.sampleTransactions(accountsModel);

        add(new AccountTabs(accountsModel, transactionsModel, openAccounts, accountSelection), CENTER);

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
