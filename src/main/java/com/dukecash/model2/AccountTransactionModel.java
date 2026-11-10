package com.dukecash.model2;

import com.dukecash.model.Account;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;

import static com.dukecash.model2.TransactionsModel.COL_ACCOUNT;
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
        if (column == COL_ACCOUNT) {
            return true;
        }
        return transactionsModel.isCellEditable(viewToSourceMapping[row], column);
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        int sourceRow = viewToSourceMapping[row];
        if (column == COL_ACCOUNT) {
            transactionsModel.updateToAccount(sourceRow, account, (Account) value);
        }
        transactionsModel.setValueAt(value, sourceRow, column);
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
        switch (ev.getType()) {
            case DELETE:
                handleSourceRowsDeleted(ev);
                return;
            case TableModelEvent.INSERT:
                handleSourceRowsInserted(ev);
                return;
            case TableModelEvent.UPDATE:
                handleUpdateSourceRow(ev);
                return;
        }

        if (ev.getFirstRow() == TableModelEvent.HEADER_ROW) {
            fireTableStructureChanged();
        } else {
            fireTableDataChanged();
        }
    }

    private void handleSourceRowsDeleted(TableModelEvent ev) {
        int delta = ev.getLastRow() - ev.getFirstRow() + 1;
        int[] build = new int[viewRowCount];
        int buildRowCount = 0;
        int updateStartRow = -1;
        int updateEndRow = -1;

        for (int viewRow = 0; viewRow < viewRowCount; viewRow++) {
            int sourceRow = viewToSourceMapping[viewRow];
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
    }

    private void handleSourceRowsInserted(TableModelEvent ev) {
        int delta = ev.getLastRow() - ev.getFirstRow() + 1;
        int[] build = new int[viewRowCount + delta];
        int buildRowCount = 0;
        int updateStartRow = -1;
        int updateEndRow = -1;
        int viewRow;
        int sourceRow;

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
    }

    private void handleUpdateSourceRow(TableModelEvent ev) {
        // If update occurs outside of visible table area, it is a full update
        if (isUpdateOutsideTableRange(ev)) {
            return;
        }

        int[] build = build = new int[viewRowCount + 1];
        int buildRowCount = 0;
        int delta = 0;
        int viewRow;
        boolean sourceInView = false;

        for (viewRow = 0; viewRow < viewRowCount; viewRow++) {
            int sourceRow = viewToSourceMapping[viewRow];
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
                sourceInView = true;
            }
            // Otherwise signal that the row has been filtered out
            else {
                fireTableRowsDeleted(viewRow, viewRow);
                delta++;
                sourceInView = true;
            }
        }

        if (!sourceInView) {
            for (viewRow = 0; viewRow < buildRowCount; viewRow++) {
                if (ev.getFirstRow() < build[viewRow]) {
                    System.arraycopy(build, viewRow, build, viewRow + 1, buildRowCount - viewRow);
                    build[viewRow] = ev.getFirstRow();
                    fireTableRowsInserted(viewRow, viewRow);
                    break;
                }
            }
            if (viewRow == buildRowCount) {
                build[viewRow] = ev.getFirstRow();
                fireTableRowsInserted(viewRow, viewRow);
            }
            buildRowCount++;
        }

        viewToSourceMapping = build;
        viewRowCount = buildRowCount;
    }

    private boolean isUpdateOutsideTableRange(TableModelEvent ev) {
        return ev.getType() == TableModelEvent.UPDATE
                && ev.getColumn() >= transactionsModel.getColumnCount()
                && (ev.getFirstRow() < 0 || ev.getFirstRow() >= transactionsModel.getRowCount());
    }
}
