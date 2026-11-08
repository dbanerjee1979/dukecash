package com.dukecash.model2;

import com.dukecash.model.Account;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.dukecash.model2.TransactionsModel.COL_ACCOUNT;
import static javax.swing.event.TableModelEvent.ALL_COLUMNS;
import static javax.swing.event.TableModelEvent.DELETE;

public class AccountTransactionModel extends AbstractTableModel {
    private static final int COL_BALANCE = 5;

    private final Account account;
    private final TransactionsModel transactionsModel;
    private int[] viewToSourceMapping;
    private int viewRowCount;

    public AccountTransactionModel(Account account, TransactionsModel transactionsModel) {
        this.account = account;
        this.transactionsModel = transactionsModel;
        rebuild();
        this.transactionsModel.addTableModelListener(this::onSourceChanged);
    }

    @Override
    public int getRowCount() {
        return viewRowCount;
    }

    @Override
    public int getColumnCount() {
        return transactionsModel.getColumnCount() + 1;
    }

    @Override
    public String getColumnName(int column) {
        if (column < transactionsModel.getColumnCount()) {
            return transactionsModel.getColumnName(column);
        }
        else if (column == COL_BALANCE) {
            return "Balance";
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column < transactionsModel.getColumnCount()) {
            return transactionsModel.getColumnClass(column);
        }
        else if (column == COL_BALANCE) {
            return BigDecimal.class;
        }
        return Object.class;
    }

    @Override
    public Object getValueAt(int row, int column) {
        int sourceRow = viewToSourceMapping[row];
        Account transfer;
        return switch (column) {
            case COL_ACCOUNT ->  (transfer = transactionsModel.findTransferAccount(sourceRow, account)) != null ? transfer : null;
            case COL_BALANCE -> BigDecimal.ZERO;
            default ->  transactionsModel.getValueAt(sourceRow, column);
        };
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return transactionsModel.isCellEditable(viewToSourceMapping[row], column);
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        transactionsModel.setValueAt(value, viewToSourceMapping[row], column);
    }

    private void rebuild() {
        viewToSourceMapping = new int[this.transactionsModel.getRowCount()];
        viewRowCount = 0;
        for (int sourceRow = 0; sourceRow < this.transactionsModel.getRowCount(); sourceRow++) {
            if (transactionsModel.hasAccount(sourceRow, account)) {
                viewToSourceMapping[viewRowCount++] = sourceRow;
            }
        }
    }

    private void onSourceChanged(TableModelEvent ev) {
        int delta = ev.getLastRow() - ev.getFirstRow() + 1;
        int sourceRow;
        int viewRow;
        int updateStartRow = -1;
        int updateEndRow = -1;
        int[] build;
        int buildRowCount = 0;
        switch (ev.getType()) {
            case DELETE:
                build = new int[viewRowCount];
                for (viewRow = 0; viewRow < viewRowCount; viewRow++) {
                    sourceRow = viewToSourceMapping[viewRow];
                    // Before start of delete range - preserve
                    if (sourceRow < ev.getFirstRow()) {
                        build[buildRowCount++] = sourceRow;
                    }
                    // Within delete range - track view row range encompassing delete range
                    else if (sourceRow <= ev.getLastRow()) {
                        if (updateStartRow == -1) {
                            updateStartRow = viewRow;
                        }
                        updateEndRow = viewRow;
                    }
                    // After end of delete range - shift by amount of source rows deleted
                    else {
                        build[buildRowCount++] = sourceRow - delta;
                    }
                }
                if (updateStartRow != -1) {
                    fireTableRowsDeleted(updateStartRow, updateEndRow);
                }
                viewToSourceMapping = build;
                viewRowCount = buildRowCount;
                return;
            case TableModelEvent.INSERT:
                build = new int[viewRowCount + delta];
                // Before start of add range, preserve
                for (viewRow = 0; viewRow < viewRowCount && (sourceRow = viewToSourceMapping[viewRow]) < ev.getFirstRow(); viewRow++) {
                    build[buildRowCount++] = sourceRow;
                }

                // Iterate add range, add transactions including the account to the account view
                for (sourceRow = ev.getFirstRow(); sourceRow <= ev.getLastRow(); sourceRow++) {
                    if (transactionsModel.hasAccount(sourceRow, account)) {
                        if (updateStartRow == -1) {
                            updateStartRow = viewRow;
                        }
                        updateEndRow = viewRow;
                        build[buildRowCount++] = sourceRow;
                    }
                }

                // After end of add range - shift by amount of source rows added
                for (; viewRow < viewRowCount; viewRow++) {
                    build[buildRowCount++] = sourceRow + delta;
                }

                if (updateStartRow != -1) {
                    fireTableRowsInserted(updateStartRow, updateEndRow);
                }
                viewToSourceMapping = build;
                viewRowCount = buildRowCount;
                return;
            case TableModelEvent.UPDATE:
                // If update occurs outside of visible table area, it is a full update
                if (isUpdateOutsideTableRange(ev)) {
                    break;
                }

                build = new int[viewRowCount];
                delta = 0;
                for (viewRow = 0; viewRow < viewRowCount; viewRow++) {
                    sourceRow = viewToSourceMapping[viewRow];
                    // Before start of update range, preserve
                    if (sourceRow < ev.getFirstRow()) {
                        build[buildRowCount++] = sourceRow;
                    }
                    // After end of update range - shift by amount of rows that got filtered out
                    else if (sourceRow > ev.getLastRow()) {
                        build[buildRowCount++] = sourceRow - delta;
                    }
                    // Within update range - keep updated rows that still include account
                    else if (transactionsModel.hasAccount(sourceRow, account)) {
                        build[buildRowCount++] = sourceRow;
                        fireTableCellUpdated(buildRowCount - 1, ev.getColumn());
                    }
                    // Otherwise signal that the row has been filtered out
                    else {
                        fireTableRowsDeleted(viewRow, viewRow);
                        delta++;
                    }
                }
                viewToSourceMapping = build;
                viewRowCount = buildRowCount;
                return;
        }

        if (ev.getFirstRow() == TableModelEvent.HEADER_ROW) {
            fireTableStructureChanged();
        } else {
            fireTableDataChanged();
        }
    }

    private boolean isUpdateOutsideTableRange(TableModelEvent ev) {
        return ev.getType() == TableModelEvent.UPDATE
                && ev.getColumn() >= transactionsModel.getColumnCount()
                && (ev.getFirstRow() < 0 || ev.getFirstRow() >= transactionsModel.getRowCount());
    }
}
