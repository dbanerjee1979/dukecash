package com.dukecash.ui;

import com.dukecash.model.Account;
import com.dukecash.model.AccountsTreeTableModel;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;

import static javax.swing.tree.TreeSelectionModel.*;

public class AccountsPanel extends JPanel {
    public AccountsPanel(AccountsTreeTableModel accountsModel, DefaultListModel<Account> openAccounts, ListSelectionModel accountSelection) {
        setLayout(new BorderLayout());

        JXTreeTable accountTree = new JXTreeTable(accountsModel);
        accountTree.setDefaultRenderer(BigDecimal.class, new CurrencyCellRenderer());
        accountTree.setSelectionMode(SINGLE_TREE_SELECTION);
        accountTree.addMouseListener(new AccountstMouseListener(accountTree, openAccounts, accountSelection));

        add(new JScrollPane(accountTree));
    }

    private static class AccountstMouseListener extends MouseAdapter {
        private final JXTreeTable accountTree;
        private final DefaultListModel<Account> openAccounts;
        private final ListSelectionModel accountSelection;

        public AccountstMouseListener(JXTreeTable accountTree, DefaultListModel<Account> openAccounts,
                                      ListSelectionModel accountSelection) {
            this.accountTree = accountTree;
            this.openAccounts = openAccounts;
            this.accountSelection = accountSelection;
        }

        @Override
        public void mouseClicked(MouseEvent ev) {
            if (!SwingUtilities.isLeftMouseButton(ev) || ev.getClickCount() != 2) {
                return;
            }

            TreePath[] selectionPaths = accountTree.getTreeSelectionModel().getSelectionPaths();
            if (selectionPaths.length == 0) {
                return;
            }

            Account account = (Account) selectionPaths[0].getLastPathComponent();
            if (account.isPlaceholder()) {
                return;
            }

            int i = openAccounts.indexOf(account);
            if (i == -1) {
                i = openAccounts.size();
                openAccounts.addElement(account);
            }
            accountSelection.addSelectionInterval(i, i);
        }
    }
}
