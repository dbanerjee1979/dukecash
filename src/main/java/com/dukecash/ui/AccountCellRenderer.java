package com.dukecash.ui;

import com.dukecash.model.Account;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class AccountCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Account account = (Account) value;

        value = account != null ? account.getName() : "-- Spit Transaction --";

        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}
