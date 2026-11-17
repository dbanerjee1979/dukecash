package com.dukecash.model;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static javax.swing.event.TableModelEvent.DELETE;

public class SortModel extends AbstractTableModel {
    private final TableModel source;
    private final int sortColumn;
    private List<Integer> viewToSourceMapping = new ArrayList<>();

    public SortModel(TableModel source, int sortColumn) {
        this.source = source;
        this.sortColumn = sortColumn;
        rebuild();
        this.source.addTableModelListener(this::onSourceChanged);
    }

    @Override
    public int getRowCount() {
        return source.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return source.getColumnCount();
    }

    @Override
    public String getColumnName(int column) {
        return source.getColumnName(column);
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return source.getColumnClass(column);
    }

    @Override
    public Object getValueAt(int row, int column) {
        int sourceRow = viewToSourceMapping.get(row);
        return source.getValueAt(sourceRow, column);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        int sourceRow = viewToSourceMapping.get(row);
        return source.isCellEditable(sourceRow, column);
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        int sourceRow = viewToSourceMapping.get(row);
        source.setValueAt(aValue, sourceRow, column);
    }

    private void rebuild() {
        viewToSourceMapping.clear();
        TreeMap<Object, List<Integer>> sourceRows = new TreeMap<>();
        for (int sourceRow = 0; sourceRow < source.getRowCount(); sourceRow++) {
            sourceRows.computeIfAbsent(source.getValueAt(sourceRow, sortColumn), k -> new ArrayList<>()).add(sourceRow);
        }
        sourceRows.values().stream().flatMap(List::stream).forEach(viewToSourceMapping::add);
    }

    private void onSourceChanged(TableModelEvent ev) {
        switch (ev.getType()) {
            case DELETE:
//                handleSourceRowsDeleted(ev);
                return;
            case TableModelEvent.INSERT:
//                handleSourceRowsInserted(ev);
                return;
            case TableModelEvent.UPDATE:
                if (handleUpdateSourceRow(ev)) {
                    return;
                }
        }

        rebuild();
        if (ev.getFirstRow() == TableModelEvent.HEADER_ROW) {
            fireTableStructureChanged();
        } else {
            fireTableDataChanged();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean handleUpdateSourceRow(TableModelEvent ev) {
        if (isUpdateOutsideTableRange(ev)) {
            return false;
        }

        int currentViewRow = viewToSourceMapping.indexOf(ev.getFirstRow());
        int viewRow = currentViewRow;
        Comparable current = (Comparable) getValueAt(viewRow, sortColumn);

        // Shift the changed row up as far as possible
        while (viewRow > 0
                && ((Comparable) getValueAt(viewRow - 1, sortColumn)).compareTo(current) > 0) {
            viewRow--;
        }

        // Shift the changed row down as far as possible
        while (viewRow < source.getRowCount() - 1
                && ((Comparable) getValueAt(viewRow + 1, sortColumn)).compareTo(current) < 0) {
            viewRow++;
        }

        // If it has moved, signal the change
        if (viewRow != currentViewRow) {
            Integer source = viewToSourceMapping.remove(currentViewRow);
            viewToSourceMapping.add(viewRow, source);
            fireTableRowsInserted(viewRow, viewRow);
            fireTableRowsDeleted(currentViewRow, currentViewRow);
        }

        return true;
    }


    private boolean isUpdateOutsideTableRange(TableModelEvent ev) {
        return ev.getType() == TableModelEvent.UPDATE
                && ev.getColumn() >= source.getColumnCount()
                && (ev.getFirstRow() < 0 || ev.getFirstRow() >= source.getRowCount());
    }
}
