package com.dukecash.ui;

import com.dukecash.model.Account;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import java.awt.event.ActionEvent;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.*;

public class WindowMenu extends JMenu implements ListDataListener {
    private final int windowInsertIndex;
    private final ButtonGroup accountButtonGroup;
    private final DefaultListModel<Account> openAccounts;
    private final ListSelectionModel accountSelection;

    public WindowMenu(DefaultListModel<Account> openAccounts, ListSelectionModel accountSelection) {
        super("Window");
        setMnemonic('W');

        this.accountButtonGroup = new ButtonGroup();

        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.setMnemonic('C');
        closeItem.setAccelerator(KeyStroke.getKeyStroke(VK_F4, CTRL_DOWN_MASK));
        closeItem.setEnabled(false);
        closeItem.addActionListener(this::closeAccount);
        accountSelection.addListSelectionListener(_ -> closeItem.setEnabled(!accountSelection.isSelectionEmpty()));
        add(closeItem);

        JRadioButtonMenuItem accountsItem = new JRadioButtonMenuItem("Accounts");
        accountsItem.setMnemonic('A');
        accountsItem.setAccelerator(KeyStroke.getKeyStroke(VK_0, CTRL_DOWN_MASK));
        accountsItem.setSelected(true);
        accountsItem.addActionListener(_ -> accountSelection.clearSelection());
        accountSelection.addListSelectionListener(_ -> accountsItem.setSelected(accountSelection.isSelectionEmpty()));
        add(accountsItem);
        accountButtonGroup.add(accountsItem);

        this.windowInsertIndex = getItemCount();
        this.openAccounts = openAccounts;
        this.accountSelection = accountSelection;
        openAccounts.addListDataListener(this);
    }

    private void closeAccount(ActionEvent ev) {
        int index = accountSelection.getMaxSelectionIndex();
        accountSelection.clearSelection();
        openAccounts.remove(index);
        if (!openAccounts.isEmpty()) {
            if (index > openAccounts.size()) {
                index = openAccounts.size() - 1;
            }
            accountSelection.addSelectionInterval(index, index);
        }
    }

    @Override
    public void intervalAdded(ListDataEvent ev) {
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            int accountIdx = i;
            Account account = openAccounts.get(i);
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(account.getName());
            menuItem.addActionListener(_ -> accountSelection.addSelectionInterval(accountIdx, accountIdx));
            accountSelection.addListSelectionListener(_ -> menuItem.setSelected(!openAccounts.isEmpty() &&
                    openAccounts.get(Math.min(Math.max(0, accountSelection.getMaxSelectionIndex()), openAccounts.size() - 1)) == account));
            accountButtonGroup.add(menuItem);
            add(menuItem, windowInsertIndex + i);
            if (i < 9) {
                menuItem.setAccelerator(KeyStroke.getKeyStroke(VK_1 + i, CTRL_DOWN_MASK));
            }
        }
    }

    @Override
    public void intervalRemoved(ListDataEvent ev) {
        int wpos = ev.getIndex0() + windowInsertIndex;
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            accountButtonGroup.remove(getItem(wpos));
            remove(wpos);
        }

        int n = Math.min(9, openAccounts.size());
        for (int i = ev.getIndex0(); i < n; i++) {
            getItem(windowInsertIndex + i).setAccelerator(KeyStroke.getKeyStroke(VK_1 + i, CTRL_DOWN_MASK));
        }
    }

    @Override
    public void contentsChanged(ListDataEvent ev) {
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            getItem(windowInsertIndex + i).setText(openAccounts.get(i).getName());
        }
    }
}
