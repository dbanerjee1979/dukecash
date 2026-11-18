package com.dukecash.model.util;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.util.Arrays;

public abstract class AbstractFilteringTableModel extends AbstractTableModel {
    protected final DefaultTableModel source;
    private int[] viewToSourceRow = new int[0];
    private int viewRowCount = 0;

    public AbstractFilteringTableModel(DefaultTableModel source) {
        this.source = source;
        rebuild();
        source.addTableModelListener(this::onSourceChanged);
    }

    protected abstract boolean isIncluded(int sourceRow);

    @Override
    public int getRowCount() {
        return viewRowCount;
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
        if (row < 0 || row >= viewRowCount) {
            throw new ArrayIndexOutOfBoundsException("index %s is out of bounds, size: %s".formatted(row, viewRowCount));
        }
        int sourceRow = viewToSourceRow[row];
        return source.getValueAt(sourceRow, column);
    }

    private void onSourceChanged(TableModelEvent ev) {
        switch (ev.getType()) {
            case TableModelEvent.DELETE:
                handleDelete(ev);
                break;
            case TableModelEvent.INSERT:
                handleInsert(ev);
                break;
            case TableModelEvent.UPDATE:
                handleUpdate(ev);
                break;
        }
    }

    private void rebuild() {
        viewToSourceRow = new int[source.getRowCount()];
        viewRowCount = 0;
        for (int sourceRow = 0; sourceRow < source.getRowCount(); sourceRow++) {
            if (isIncluded(sourceRow)) {
                viewToSourceRow[viewRowCount++] = sourceRow;
            }
        }
    }

    private void handleInsert(TableModelEvent ev) {
        int[] build = new int[source.getRowCount()];
        int buildRowCount = 0;
        int delta = ev.getLastRow() - ev.getFirstRow() + 1;
        int sourceRow;
        int viewRow;
        for (viewRow = 0; viewRow < viewRowCount && (sourceRow = viewToSourceRow[viewRow]) < ev.getFirstRow(); viewRow++) {
            build[buildRowCount++] = sourceRow;
        }
        int insertedStart = -1;
        int insertedEnd = -1;
        for (sourceRow = ev.getFirstRow(); sourceRow <= ev.getLastRow(); sourceRow++) {
            if (isIncluded(sourceRow)) {
                insertedEnd = buildRowCount;
                if (insertedStart == -1) {
                    insertedStart = buildRowCount;
                }
                build[buildRowCount++] = sourceRow;
            }
        }
        for (; viewRow < viewRowCount; viewRow++) {
            build[buildRowCount++] = viewToSourceRow[viewRow] + delta;
        }
        viewToSourceRow = build;
        viewRowCount = buildRowCount;
        if (insertedStart != -1) {
            fireTableRowsInserted(insertedStart, insertedEnd);
        }
    }

    private void handleDelete(TableModelEvent ev) {
        int[] build = new int[source.getRowCount()];
        int buildRowCount = 0;
        int delta = ev.getLastRow() - ev.getFirstRow() + 1;
        int deletedStart = -1;
        int deletedEnd = -1;
        for (int viewRow = 0; viewRow < viewRowCount; viewRow++) {
            int sourceRow = viewToSourceRow[viewRow];
            if (sourceRow < ev.getFirstRow()) {
                build[buildRowCount++] = sourceRow;
            } else if (sourceRow > ev.getLastRow()) {
                build[buildRowCount++] = sourceRow - delta;
            } else {
                deletedEnd = viewRow;
                if (deletedStart == -1) {
                    deletedStart = viewRow;
                }
            }
        }
        viewToSourceRow = build;
        viewRowCount = buildRowCount;
        if (deletedStart != -1) {
            fireTableRowsDeleted(deletedStart, deletedEnd);
        }
    }

    private void handleUpdate(TableModelEvent ev) {
        if (ev.getFirstRow() == TableModelEvent.HEADER_ROW) {
            rebuild();
            fireTableStructureChanged();
            return;
        }

        for (int sourceRow = ev.getFirstRow(); sourceRow <= ev.getLastRow(); sourceRow++) {
            if (!isIncluded(sourceRow)) {
                int viewRow = Arrays.binarySearch(viewToSourceRow, sourceRow);
                viewRowCount--;
                System.arraycopy(viewToSourceRow, viewRow + 1, viewToSourceRow, viewRow, viewRowCount - viewRow);
                fireTableRowsDeleted(viewRow, viewRow);
            } else {
                int viewRow;
                for (viewRow = 0; viewRow < viewRowCount && viewToSourceRow[viewRow] < sourceRow; viewRow++) {
                    // Skip past rows preceding the change
                }
                int[] build = new int[viewRowCount + 1];
                System.arraycopy(viewToSourceRow, 0, build, 0, viewRow);
                build[viewRow] = sourceRow;
                System.arraycopy(viewToSourceRow, viewRow, build, viewRow + 1, viewRowCount - viewRow);
                viewToSourceRow = build;
                viewRowCount++;
                fireTableRowsInserted(viewRow, viewRow);
            }
        }
    }
}