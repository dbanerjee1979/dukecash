package com.dukecash.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class CurrencyCellRenderer extends DefaultTableCellRenderer {
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00;(#,##0.00)");

    private Color defaultForegroundColor;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (defaultForegroundColor == null) {
            defaultForegroundColor = component.getForeground();
        }
        setForeground(defaultForegroundColor);
        setHorizontalAlignment(SwingConstants.RIGHT);

        if (value instanceof BigDecimal amount) {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                setForeground(Color.RED);
            }
            setText(CURRENCY_FORMAT.format(amount));
        }
        return component;
    }
}
