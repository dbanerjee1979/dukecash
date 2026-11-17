package com.dukecash.ui;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.time.*;
import java.util.Date;
import java.text.ParseException;
import java.util.EventObject;

public class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JSpinner spinner;

    public SpinnerCellEditor(SpinnerDateModel model) {
        this.spinner = new JSpinner(model) {
            @Override
            protected JComponent createEditor(SpinnerModel model) {
                return new DateEditor(this, "yyyy-MM-dd");
            }
        };

        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        editor.getTextField().setColumns(5);
        editor.getTextField().addActionListener(_ -> stopCellEditing());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        spinner.setValue(Date.from(Instant.from(ZonedDateTime.of(((LocalDate) value).atStartOfDay(), ZoneId.systemDefault()))));
        return spinner;
    }

    @Override
    public Object getCellEditorValue() {
        return ((Date) spinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public boolean stopCellEditing() {
        try {
            spinner.commitEdit();
        } catch (ParseException e) {
            // Invalid value
        }
        return super.stopCellEditing();
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
}
