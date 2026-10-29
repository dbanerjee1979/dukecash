package com.dukecash.ui;

import com.dukecash.model.Account;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.*;

public class WindowMenuManager implements ListDataListener, ListSelectionListener {
    private final JMenu windowMenu;
    private final int windowInsertIndex;
    private final ButtonGroup accountButtonGroup;
    private final DefaultListModel<Account> openAccounts;
    private final ListSelectionModel accountSelection;

    public WindowMenuManager(JMenu windowMenu,
                             DefaultListModel<Account> openAccounts, ListSelectionModel accountSelection) {
        this.windowMenu = windowMenu;
        this.accountButtonGroup = new ButtonGroup();

        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.setMnemonic('C');
        closeItem.setAccelerator(KeyStroke.getKeyStroke(VK_F4, CTRL_DOWN_MASK));
        closeItem.setEnabled(false);
        closeItem.addActionListener(_ -> openAccounts.remove(accountSelection.getMaxSelectionIndex()));
        accountSelection.addListSelectionListener(_ -> closeItem.setEnabled(!accountSelection.isSelectionEmpty()));
        windowMenu.add(closeItem);

        JRadioButtonMenuItem accountsItem = new JRadioButtonMenuItem("Accounts");
        accountsItem.setMnemonic('A');
        accountsItem.setAccelerator(KeyStroke.getKeyStroke(VK_0, CTRL_DOWN_MASK));
        accountsItem.setSelected(true);
        accountsItem.addActionListener(_ -> accountSelection.clearSelection());
        accountSelection.addListSelectionListener(_ -> accountsItem.setSelected(accountSelection.isSelectionEmpty()));
        windowMenu.add(accountsItem);
        accountButtonGroup.add(accountsItem);

        this.windowInsertIndex = windowMenu.getItemCount();
        this.openAccounts = openAccounts;
        this.accountSelection = accountSelection;
        openAccounts.addListDataListener(this);
        accountSelection.addListSelectionListener(this);
    }

    @Override
    public void intervalAdded(ListDataEvent ev) {
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            int account = i;
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(openAccounts.get(i).getName());
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
        int wpos = ev.getIndex0() + windowInsertIndex;
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            accountButtonGroup.remove(windowMenu.getItem(wpos));
            windowMenu.remove(wpos);
        }

        int n = Math.min(9, openAccounts.size());
        for (int i = ev.getIndex0(); i < n; i++) {
            windowMenu.getItem(windowInsertIndex + i).setAccelerator(KeyStroke.getKeyStroke(VK_1 + i, CTRL_DOWN_MASK));
        }
    }

    @Override
    public void contentsChanged(ListDataEvent ev) {
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            windowMenu.getItem(windowInsertIndex + i).setText(openAccounts.get(i).getName());
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent ev) {
        if (accountSelection.getSelectedItemsCount() > 0) {
            int i = accountSelection.getMaxSelectionIndex();
            windowMenu.getItem(windowInsertIndex + i).setSelected(true);
        }
    }
}
