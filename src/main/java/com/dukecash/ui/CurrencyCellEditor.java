package com.dukecash.ui;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

class CurrencyCellEditor extends DefaultCellEditor {
    public CurrencyCellEditor() {
        super(new JTextField());
        ((JTextField) editorComponent).setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public Object getCellEditorValue() {
        try {
            return new BigDecimal((String) super.getCellEditorValue());
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
