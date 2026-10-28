package com.dukecash.ui;

import com.dukecash.model.Account;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TabsManager implements ListDataListener, ListSelectionListener {
    private final JTabbedPane tabs;
    private final DefaultListModel<Account> openAccounts;
    private final ListSelectionModel accountSelection;

    public TabsManager(JTabbedPane tabs, DefaultListModel<Account> openAccounts,
                       ListSelectionModel accountSelection) {
        this.tabs = tabs;
        this.openAccounts = openAccounts;
        this.accountSelection = accountSelection;
        openAccounts.addListDataListener(this);
        accountSelection.addListSelectionListener(this);
    }

    @Override
    public void intervalAdded(ListDataEvent ev) {
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            int tab = i + 1;
            tabs.add(new JLabel("Transactions"), tab);
            String accountName = openAccounts.get(i).getName();
            tabs.setTitleAt(tab, accountName);
        }
    }

    @Override
    public void intervalRemoved(ListDataEvent ev) {
        int tab = ev.getIndex0() + 1;
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            tabs.remove(tab);
        }

        accountSelection.clearSelection();
        if (ev.getIndex0() < openAccounts.size()) {
            accountSelection.addSelectionInterval(ev.getIndex0(), ev.getIndex0());
        }
    }

    @Override
    public void contentsChanged(ListDataEvent ev) {
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            tabs.setTitleAt(i + 1, openAccounts.get(i).getName());
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent ev) {
        int t = accountSelection.getSelectedItemsCount() == 0 ? 0 : accountSelection.getMaxSelectionIndex() + 1;
        if (tabs.getSelectedIndex() != t) {
            tabs.setSelectedIndex(t);
        }
    }
}
