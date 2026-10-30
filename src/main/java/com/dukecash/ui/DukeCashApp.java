package com.dukecash.ui;

import com.dukecash.model.Account;

import javax.swing.*;
import javax.swing.event.*;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class DukeCashApp extends JFrame {
    private final DefaultListModel<Account> openAccounts = new DefaultListModel<>();
    private final ListSelectionModel accountSelection = new DefaultListSelectionModel();

    private final JTabbedPane tabs = new JTabbedPane();
    private final TabsManager tabsManager;

    public DukeCashApp() {
        super("DukeCash");

        accountSelection.setSelectionMode(SINGLE_SELECTION);

        add(tabs, CENTER);
        tabs.addChangeListener(this::onTabsChanged);

        tabs.add("Accounts", new AccountsPanel(openAccounts, accountSelection));

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(new WindowMenu(openAccounts, accountSelection));
        add(menuBar, NORTH);

        setSize(1024, 768);

        this.tabsManager = new TabsManager(tabs, openAccounts, accountSelection);
    }

    private void onTabsChanged(ChangeEvent ev) {
        if (tabs.getSelectedIndex() == 0) {
            accountSelection.clearSelection();
        } else {
            int i = tabs.getSelectedIndex() - 1;
            if (accountSelection.getMaxSelectionIndex() != i) {
                accountSelection.addSelectionInterval(i, i);
            }
        }
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
