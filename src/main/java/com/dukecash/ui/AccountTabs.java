package com.dukecash.ui;

import com.dukecash.model.*;

import javax.swing.*;
import javax.swing.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AccountTabs extends JTabbedPane implements ListDataListener, ListSelectionListener {
    private final AccountsTreeTableModel accountsModel;
    private final TransactionsModel transactionsModel;
    private final DefaultListModel<Account> openAccounts;
    private final ListSelectionModel accountSelection;

    public AccountTabs(AccountsTreeTableModel accountsModel, TransactionsModel transactionsModel,
                       DefaultListModel<Account> openAccounts, ListSelectionModel accountSelection) {
        this.accountsModel = accountsModel;
        this.transactionsModel = transactionsModel;
        this.openAccounts = openAccounts;
        this.accountSelection = accountSelection;
        add("Accounts", new AccountsPanel(accountsModel, openAccounts, accountSelection));
        openAccounts.addListDataListener(this);
        accountSelection.addListSelectionListener(this);
        addChangeListener(this::onTabsChanged);
    }

    @Override
    public void intervalAdded(ListDataEvent ev) {
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            int tab = i + 1;
            Account account = openAccounts.get(i);

            JTable table = new JTable(new SortingTableModel(new AccountTransactionModel(account, transactionsModel),
                    TransactionsModel.COL_DATE));
            table.setDefaultRenderer(BigDecimal.class, new CurrencyCellRenderer());
            table.setDefaultEditor(BigDecimal.class, new CurrencyCellEditor());
            table.setDefaultRenderer(Account.class, new AccountCellRenderer());
            table.setDefaultEditor(Account.class, new AccountCellEditor(account, accountsModel));
            table.setDefaultEditor(LocalDate.class, new SpinnerCellEditor(new SpinnerDateModel()));
            add(new JScrollPane(table), tab);

            String accountName = account.getName();
            setTitleAt(tab, accountName);
        }
    }

    @Override
    public void intervalRemoved(ListDataEvent ev) {
        int tab = ev.getIndex0() + 1;
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            remove(tab);
        }

        accountSelection.clearSelection();
        if (ev.getIndex0() < openAccounts.size()) {
            accountSelection.addSelectionInterval(ev.getIndex0(), ev.getIndex0());
        }
    }

    @Override
    public void contentsChanged(ListDataEvent ev) {
        for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
            setTitleAt(i + 1, openAccounts.get(i).getName());
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent ev) {
        int t = accountSelection.getSelectedItemsCount() == 0 ? 0 :
                Math.min(accountSelection.getMaxSelectionIndex() + 1, getTabCount() - 1);
        if (getSelectedIndex() != t) {
            setSelectedIndex(t);
        }
    }

    private void onTabsChanged(ChangeEvent ev) {
        if (getSelectedIndex() == 0) {
            accountSelection.clearSelection();
        } else {
            int i = getSelectedIndex() - 1;
            if (accountSelection.getMaxSelectionIndex() != i) {
                accountSelection.addSelectionInterval(i, i);
            }
        }
    }
}
