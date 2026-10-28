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
    private final TabsManager tabsManager;
    private final WindowMenuManager windowMenuManager;

    public DukeCashApp() {
        super("DukeCash");

        accountSelection.setSelectionMode(SINGLE_SELECTION);

        add(tabs, CENTER);
        tabs.addChangeListener(this::onTabsChanged);

        tabs.add("Accounts", new AccountsPanel(openAccounts, accountSelection));
        this.tabsManager = new TabsManager(tabs, openAccounts, accountSelection);

        JMenuItem closeItem;
        JRadioButtonMenuItem accountsItem;
        JMenu windowMenu;
        ButtonGroup accountButtonGroup = new ButtonGroup();
        add(menuBar(windowMenu = menu("Window", 'W',
                        closeItem = menuItem("Close", 'C', this::onAccountClose,
                                KeyStroke.getKeyStroke(VK_F4, CTRL_DOWN_MASK)),
                        accountsItem = radioMenuItem("Accounts", 'A', _ -> accountSelection.clearSelection(),
                                KeyStroke.getKeyStroke(VK_0, CTRL_DOWN_MASK), accountButtonGroup))),
                NORTH);

        this.windowMenuManager = new WindowMenuManager(windowMenu, accountButtonGroup, openAccounts, accountSelection);

        closeItem.setEnabled(false);
        accountSelection.addListSelectionListener(_ -> closeItem.setEnabled(!accountSelection.isSelectionEmpty()));

        accountsItem.setSelected(true);
        accountSelection.addListSelectionListener(_ -> accountsItem.setSelected(accountSelection.isSelectionEmpty()));

        setSize(1024, 768);
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
