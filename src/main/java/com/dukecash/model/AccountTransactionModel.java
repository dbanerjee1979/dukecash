package com.dukecash.model;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static com.dukecash.model.TransactionsModel.COL_ACCOUNT;
import static javax.swing.event.TableModelEvent.DELETE;

public class AccountTransactionModel extends AbstractTableModel {
    private static final int COL_BALANCE = 5;
    public static final int COL_AMOUNT = 6;
    public static final int COL_PAYMENT_TYPE = 7;

    private final Account account;
    private final TransactionsModel transactionsModel;
    private final List<Integer> viewToSourceMapping = new ArrayList<>();

    public AccountTransactionModel(Account account, TransactionsModel transactionsModel) {
        this.account = account;
        this.transactionsModel = transactionsModel;
        rebuild();
        this.transactionsModel.addTableModelListener(this::onSourceChanged);
    }

    @Override
    public int getRowCount() {
        return viewToSourceMapping.size();
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
        int sourceRow = viewToSourceMapping.get(row);
        Account transfer = transactionsModel.findTransferAccount(sourceRow, account);
        Transaction transaction = (Transaction) transactionsModel.getValueAt(sourceRow, TransactionsModel.COL_TRANSACTION);
        return switch (column) {
            case COL_ACCOUNT -> transfer;
            case COL_BALANCE -> BigDecimal.ZERO;
            case COL_AMOUNT -> transaction.findAmountForAccount(account);
            case COL_PAYMENT_TYPE -> transaction.findPaymentTypeForAccount(account);
            default ->  transactionsModel.getValueAt(sourceRow, column);
        };
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == COL_ACCOUNT) {
            return true;
        }
        return transactionsModel.isCellEditable(viewToSourceMapping.get(row), column);
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        int sourceRow = viewToSourceMapping.get(row);
        if (column == COL_ACCOUNT) {
            transactionsModel.updateToAccount(sourceRow, account, (Account) value);
        } else {
            transactionsModel.setValueAt(value, sourceRow, column);
        }
    }

    private void rebuild() {
        viewToSourceMapping.clear();
        for (int sourceRow = 0; sourceRow < this.transactionsModel.getRowCount(); sourceRow++) {
            if (transactionsModel.hasAccount(sourceRow, account)) {
                viewToSourceMapping.add(sourceRow);
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

    private void handleSourceRowsDeleted(TableModelEvent ev) {
        ListIterator<Integer> viewRowIterator = viewToSourceMapping.listIterator();
        int delta = ev.getLastRow() - ev.getFirstRow() + 1;

        int deleteStart = -1;
        while (viewRowIterator.hasNext()) {
             if (viewRowIterator.next() >= ev.getFirstRow()) {
                 deleteStart = viewRowIterator.previousIndex();
                 viewRowIterator.previous();
                 break;
             }
        }

        int deleteEnd = -1;
        while (viewRowIterator.hasNext() && viewRowIterator.next() <= ev.getLastRow()) {
            deleteEnd = viewRowIterator.previousIndex();
            viewRowIterator.remove();
        }

        if (deleteStart != -1) {
            fireTableRowsDeleted(deleteStart, deleteEnd);
            viewRowIterator.previous();
        }

        while (viewRowIterator.hasNext()) {
            viewRowIterator.set(viewRowIterator.next() - delta);
        }
    }

    private void handleSourceRowsInserted(TableModelEvent ev) {
        ListIterator<Integer> viewRowIterator = viewToSourceMapping.listIterator();
        int delta = ev.getLastRow() - ev.getFirstRow() + 1;

        while (viewRowIterator.hasNext()) {
            if (viewRowIterator.next() >= ev.getFirstRow()) {
                viewRowIterator.previous();
                break;
            }
        }

        for (int sourceRow = ev.getFirstRow(); sourceRow <= ev.getLastRow(); sourceRow++) {
            if (transactionsModel.hasAccount(sourceRow, account)) {
                int insertIndex = viewRowIterator.nextIndex();
                viewRowIterator.add(sourceRow);
                fireTableRowsInserted(insertIndex, insertIndex);
            }
        }

        while (viewRowIterator.hasNext()) {
            viewRowIterator.set(viewRowIterator.next() + delta);
        }
    }

    private boolean handleUpdateSourceRow(TableModelEvent ev) {
        // If update occurs outside of visible table area, it is a full update
        if (isUpdateOutsideTableRange(ev)) {
            return false;
        }

        ListIterator<Integer> viewRowIterator = viewToSourceMapping.listIterator();
        int viewIndex;
        for (viewIndex = 0; viewRowIterator.hasNext(); viewIndex++) {
            if (viewRowIterator.next() >= ev.getFirstRow()) {
                viewRowIterator.previous();
                break;
            }
        }

        for (int sourceRow = ev.getFirstRow(); sourceRow <= ev.getLastRow(); sourceRow++) {
            if (viewRowIterator.hasNext() && viewRowIterator.next() == sourceRow) {
                if (transactionsModel.hasAccount(sourceRow, account)) {
                    fireTableCellUpdated(viewIndex, ev.getColumn());
                    viewIndex++;
                } else {
                    viewRowIterator.remove();
                    fireTableRowsDeleted(viewIndex, viewIndex);
                }
            } else if (transactionsModel.hasAccount(sourceRow, account)) {
                viewRowIterator.add(sourceRow);
                fireTableRowsInserted(viewIndex, viewIndex);
                viewIndex++;
            }
        }
        return true;
    }

    private boolean isUpdateOutsideTableRange(TableModelEvent ev) {
        return ev.getType() == TableModelEvent.UPDATE
                && ev.getColumn() >= transactionsModel.getColumnCount()
                && (ev.getFirstRow() < 0 || ev.getFirstRow() >= transactionsModel.getRowCount());
    }
}
