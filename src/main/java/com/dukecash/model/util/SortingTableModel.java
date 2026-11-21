package com.dukecash.model.util;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import javax.swing.table.TableModel;
import java.util.*;

import static javax.swing.event.TableModelEvent.*;

public class SortingTableModel extends AbstractTableModel {
    private final TableModel source;
    private Comparator<Integer> rowComparator;
    private int[] viewToSourceRow = new int[0];
    private int viewRowCount = 0;

    public SortingTableModel(TableModel source, int sortColumn, boolean ascending) {
        this.source = source;
        setSorting(sortColumn, ascending);
        source.addTableModelListener(this::onSourceChanged);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setSorting(int sortColumn, boolean ascending) {
        this.rowComparator = Comparator.comparing(sourceRow -> (Comparable) this.source.getValueAt(sourceRow, sortColumn));
        this.rowComparator = ascending ? rowComparator : rowComparator.reversed();
        rebuild();
        fireTableDataChanged();
    }

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
    public Class<?> getColumnClass(int columnIndex) {
        return source.getColumnClass(columnIndex);
    }

    @Override
    public Object getValueAt(int row, int column) {
        return source.getValueAt(viewToSourceRow[Objects.checkIndex(row, getRowCount())], column);
    }

    private void onSourceChanged(TableModelEvent ev) {
        switch (ev.getType()) {
            case DELETE:
                handleDelete(ev);
                break;
            case INSERT:
                handleInsert(ev);
                break;
            case UPDATE:
                handleUpdate(ev);
                break;
        }
    }

    private void handleInsert(TableModelEvent ev) {
        int[] buildRows = new int[source.getRowCount()];
        int delta = ev.getLastRow() - ev.getFirstRow() + 1;
        for (int sourceRow = ev.getFirstRow(); sourceRow <= ev.getLastRow(); sourceRow++) {
            int buildRowCount = 0;
            int insertRow;
            int existingSourceRow;
            int viewRow;

            for (viewRow = 0; viewRow < viewRowCount && rowComparator.compare((existingSourceRow = viewToSourceRow[viewRow]), sourceRow) <= 0; viewRow++) {
                int updateDelta = existingSourceRow >= sourceRow ? delta /* adjust trailing */ : 0;
                buildRows[buildRowCount++] = existingSourceRow + updateDelta;
            }

            insertRow = viewRow;
            buildRows[buildRowCount++] = sourceRow;

            for (; viewRow < viewRowCount; viewRow++) {
                existingSourceRow = viewToSourceRow[viewRow];
                int updateDelta = existingSourceRow >= sourceRow ? delta /* adjust trailing */ : 0;
                buildRows[buildRowCount++] = existingSourceRow + updateDelta;
            }

            viewToSourceRow = buildRows;
            viewRowCount = buildRowCount;
            fireTableRowsInserted(insertRow, insertRow);
        }
    }

    private void handleDelete(TableModelEvent ev) {
        int delta = ev.getLastRow() - ev.getFirstRow() + 1;
        for (int sourceRow = ev.getFirstRow(); sourceRow <= ev.getLastRow(); sourceRow++) {
            int viewRow;
            int deleteRow = -1;
            for (viewRow = 0; viewRow < viewRowCount; viewRow++) {
                int existingSourceRow = viewToSourceRow[viewRow];
                if (existingSourceRow == sourceRow) {
                    deleteRow = viewRow;
                } else {
                    int updateViewRow = deleteRow >= 0 ? viewRow - 1 /* shift up */ : viewRow;
                    int updateDelta = existingSourceRow > sourceRow ? delta /* adjust trailing */ : 0;
                    viewToSourceRow[updateViewRow] = existingSourceRow - updateDelta;
                }
            }
            viewRowCount--;
            fireTableRowsDeleted(deleteRow, deleteRow);
        }
    }

    private void handleUpdate(TableModelEvent ev) {
        if (ev.getFirstRow() == HEADER_ROW) {
            rebuild();
            fireTableStructureChanged();
            return;
        }

        if (ev.getLastRow() == Integer.MAX_VALUE) {
            rebuild();
            fireTableDataChanged();
            return;
        }

        nextSource: for (int sourceRow = ev.getFirstRow(); sourceRow <= ev.getLastRow(); sourceRow++) {
            for (int existingViewRow = 0; existingViewRow < viewRowCount; existingViewRow++) {
                if (viewToSourceRow[existingViewRow] == sourceRow) {
                    for (int viewRow = existingViewRow - 1; viewRow > 0; viewRow--) {
                        if (rowComparator.compare(viewToSourceRow[viewRow], sourceRow) > 0) {
                            System.arraycopy(viewToSourceRow, viewRow, viewToSourceRow, viewRow + 1, existingViewRow - viewRow);
                            viewToSourceRow[viewRow] = sourceRow;
                            fireTableRowsDeleted(existingViewRow, existingViewRow);
                            fireTableRowsInserted(viewRow, viewRow);
                            break nextSource;
                        }
                    }

                    for (int viewRow = existingViewRow + 1; viewRow < viewRowCount; viewRow++) {
                        if (rowComparator.compare(viewToSourceRow[viewRow], sourceRow) < 0) {
                            System.arraycopy(viewToSourceRow, existingViewRow + 1, viewToSourceRow, existingViewRow, viewRow - existingViewRow);
                            viewToSourceRow[viewRow] = sourceRow;
                            fireTableRowsDeleted(existingViewRow, existingViewRow);
                            fireTableRowsInserted(viewRow, viewRow);
                            break nextSource;
                        }
                    }

                    fireTableCellUpdated(existingViewRow, ev.getColumn());
                }
            }
        }
    }

    private void rebuild() {
        viewToSourceRow = new int[source.getRowCount()];

        TreeSet<Integer> sortedSourceRows = new TreeSet<>(rowComparator);
        for (int sourceRow = 0; sourceRow < source.getRowCount(); sourceRow++) {
            sortedSourceRows.add(sourceRow);
        }

        viewRowCount = 0;
        for (Integer sortedSourceRow : sortedSourceRows) {
            viewToSourceRow[viewRowCount++] = sortedSourceRow;
        }
    }
}
