package com.dukecash.ui;

import com.dukecash.model.AccountsTreeTableModel;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class AccountsPanel extends JPanel {
    public AccountsPanel() {
        setLayout(new BorderLayout());

        JXTreeTable accountTree = new JXTreeTable(new AccountsTreeTableModel());
        accountTree.setDefaultRenderer(BigDecimal.class, new CurrencyCellRenderer());

        add(new JScrollPane(accountTree));
    }
}
