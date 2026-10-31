package com.dukecash.model;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.function.Predicate;

public class FilterTableModel extends AbstractTableModel {
    private final TableModel source;
    private final Predicate<Integer> rowFilter;
    private int[] filteredRows = new int[0];
    private int size;

    public FilterTableModel(TableModel source, Predicate<Integer> rowFilter) {
        this.source = source;
        this.source.addTableModelListener(_ -> this.rebuild());
        this.rowFilter = rowFilter;
        this.rebuild();
    }

    @Override
    public int getRowCount() {
        return size;
    }

    @Override
    public int getColumnCount() {
        return this.source.getColumnCount();
    }

    @Override
    public String getColumnName(int column) {
        return source.getColumnName(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return source.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return source.isCellEditable(rowIndex, columnIndex);
    }

    @Override
    public Object getValueAt(int row, int column) {
        checkRow(row);
        return source.getValueAt(filteredRows[row], column);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        source.setValueAt(aValue, rowIndex, columnIndex);
    }

    private void rebuild() {
        filteredRows = new int[this.source.getRowCount()];
        size = 0;
        for (int i = 0; i < this.source.getRowCount(); i++) {
            if (rowFilter.test(i)) {
                filteredRows[size++] = i;
            }
        }
        fireTableDataChanged();
    }

    private void checkRow(int row) {
        if (row < 0 || row >= size) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }
}
