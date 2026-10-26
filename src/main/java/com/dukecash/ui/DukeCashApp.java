package com.dukecash.ui;

import com.dukecash.model.Account;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.*;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class DukeCashApp extends JFrame {
    private final DefaultListModel<Account> openAccounts = new DefaultListModel<>();
    private final ListSelectionModel accountSelection = new DefaultListSelectionModel();

    private final JTabbedPane tabs = new JTabbedPane();
    private final ButtonGroup accountButtonGroup = new ButtonGroup();
    private final JMenu windowMenu;
    private final int windowInsertIndex;

    public DukeCashApp() {
        super("DukeCash");

        tabs.add("Accounts", new AccountsPanel(openAccounts, accountSelection));

        JMenuItem closeItem;
        JRadioButtonMenuItem accountsItem;
        add(menuBar(windowMenu = menu("Window", 'W',
                        closeItem = menuItem("Close", 'C', this::onAccountClose,
                                KeyStroke.getKeyStroke(VK_F4, CTRL_DOWN_MASK)),
                        accountsItem = radioMenuItem("Accounts", 'A', _ -> accountSelection.clearSelection(),
                                KeyStroke.getKeyStroke(VK_0, CTRL_DOWN_MASK), accountButtonGroup))),
                NORTH);
        this.windowInsertIndex = windowMenu.getItemCount();
        closeItem.setEnabled(false);
        accountSelection.addListSelectionListener(_ -> closeItem.setEnabled(!accountSelection.isSelectionEmpty()));
        accountsItem.setSelected(true);
        accountSelection.addListSelectionListener(_ -> accountsItem.setSelected(accountSelection.isSelectionEmpty()));

        add(tabs, CENTER);

        setSize(1024, 768);

        tabs.addChangeListener(this::onTabsChanged);

        accountSelection.setSelectionMode(SINGLE_SELECTION);
        accountSelection.addListSelectionListener(this::onSelectionChanged);
        openAccounts.addListDataListener(new OpenAccountsListener());
    }

    private JMenuBar menuBar(JMenu... menus) {
        JMenuBar menuBar = new JMenuBar();
        for (JMenu menu : menus) {
            menuBar.add(menu);
        }
        return menuBar;
    }

    private JMenu menu(String title, char mnemonic, JMenuItem... items) {
        JMenu menu = new JMenu(title);
        menu.setMnemonic(mnemonic);
        for (JMenuItem item : items) {
            menu.add(item);
        }
        return menu;
    }

    private JMenuItem menuItem(String title, char mnemonic, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.setMnemonic(mnemonic);
        menuItem.addActionListener(actionListener);
        return menuItem;
    }

    private JMenuItem menuItem(String title, char mnenomic, ActionListener actionListener, KeyStroke accelerator) {
        JMenuItem menuItem = menuItem(title, mnenomic, actionListener);
        menuItem.setAccelerator(accelerator);
        return menuItem;
    }

    private JRadioButtonMenuItem radioMenuItem(String title, char mnenomic, ActionListener actionListener, KeyStroke accelerator, ButtonGroup buttonGroup) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(title);
        menuItem.setMnemonic(mnenomic);
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(accelerator);
        buttonGroup.add(menuItem);
        return menuItem;
    }

    private void onAccountClose(ActionEvent ev) {
        openAccounts.remove(accountSelection.getMaxSelectionIndex());
    }

    private void onTabsChanged(ChangeEvent ev) {
        if (tabs.getSelectedIndex() == 0) {
            accountSelection.clearSelection();;
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

    private void onSelectionChanged(ListSelectionEvent ev) {
        int t;
        if (accountSelection.getSelectedItemsCount() == 0) {
            t = 0;
            accountButtonGroup.clearSelection();
        } else {
            int i = accountSelection.getMaxSelectionIndex();
            t = i + 1;
            windowMenu.getItem(windowInsertIndex + i).setSelected(true);
        }

        if (tabs.getSelectedIndex() != t) {
            tabs.setSelectedIndex(t);
        }
    }

    private class OpenAccountsListener implements ListDataListener {
        @Override
        public void intervalAdded(ListDataEvent ev) {
            for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
                int account = i;
                int tab = i + 1;
                tabs.add(new JLabel("Transactions"), tab);
                String accountName = openAccounts.get(i).getName();
                tabs.setTitleAt(tab, accountName);
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(accountName);
                menuItem.addActionListener(_ -> accountSelection.addSelectionInterval(account, account));
                accountButtonGroup.add(menuItem);
                windowMenu.add(menuItem, windowInsertIndex + i);
                if (i < 9) {
                    menuItem.setAccelerator(KeyStroke.getKeyStroke(VK_1 + i, CTRL_DOWN_MASK));
                }
            }
        }

        @Override
        public void intervalRemoved(ListDataEvent ev) {
            int tab = ev.getIndex0() + 1;
            int wpos = ev.getIndex0() + windowInsertIndex;
            for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
                tabs.remove(tab);
                accountButtonGroup.remove(windowMenu.getItem(wpos));
                windowMenu.remove(wpos);
            }

            accountSelection.clearSelection();
            if (ev.getIndex0() < openAccounts.size()) {
                accountSelection.addSelectionInterval(ev.getIndex0(), ev.getIndex0());
            }

            int n = Math.min(9, openAccounts.size());
            for (int i = ev.getIndex0(); i < n; i++) {
                windowMenu.getItem(windowInsertIndex + i).setAccelerator(KeyStroke.getKeyStroke(VK_1 + i, CTRL_DOWN_MASK));
            }
        }

        @Override
        public void contentsChanged(ListDataEvent ev) {
            for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
                tabs.setTitleAt(i + 1, openAccounts.get(i).getName());
            }
        }
    }
}
