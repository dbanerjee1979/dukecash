package com.dukecash.ui;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.*;
import java.util.Date;
import java.text.ParseException;
import java.util.EventObject;

public class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor {
    private final JSpinner spinner;
    private JTable table;

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
        this.table = table;
        // Temporarily disable table arrow key traversal during editing
        disableArrowKeyTraversal();
        SwingUtilities.invokeLater(this::grabFocus);
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
        restoreTableKeyBindings();
        return super.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        restoreTableKeyBindings();
        super.cancelCellEditing();
    }

    @Override
    public boolean shouldSelectCell(EventObject ev) {
        return true;
    }

    private void disableArrowKeyTraversal() {
        if (table != null) {
            InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "none");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
        }
    }

    private void restoreTableKeyBindings() {
        if (table != null) {
            InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "selectPreviousRow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "selectNextRow");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "selectPreviousColumnCell");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "selectNextColumnCell");
        }
    }

    private void grabFocus() {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().requestFocusInWindow();
        }
    }
}
