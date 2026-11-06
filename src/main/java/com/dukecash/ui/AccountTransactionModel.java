package com.dukecash.ui;

import com.dukecash.model.Account;
import com.dukecash.model2.TransactionsModel;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static com.dukecash.model2.TransactionsModel.COL_ACCOUNT;

public class AccountTransactionModel extends AbstractTableModel {
    private static final int COL_BALANCE = 5;

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
        Account transfer;
        return switch (column) {
            case COL_ACCOUNT ->  (transfer = transactionsModel.findTransferAccount(sourceRow, account)) != null ? transfer : null;
            case COL_BALANCE -> BigDecimal.ZERO;
            default ->  transactionsModel.getValueAt(sourceRow, column);
        };
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
            case TableModelEvent.DELETE:
                removeAffectedRange(ev);
                return;
            case TableModelEvent.INSERT, TableModelEvent.UPDATE:
                if (isUpdateOutsideTableRange(ev)) {
                    break;
                }
                ListIterator<Integer> insertion = removeAffectedRange(ev);
                insertToView(ev, insertion);
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

    private ListIterator<Integer> removeAffectedRange(TableModelEvent ev) {
        ListIterator<Integer> i = viewToSourceMapping.listIterator();
        int viewRow = -1;
        int startRow = -1;
        while (i.hasNext() && (viewRow = i.next()) <= ev.getLastRow()) {
            if (viewRow >= ev.getFirstRow()) {
                i.remove();
                if (startRow == -1) {
                    startRow = viewRow;
                }
            }
        }
        if (startRow != -1) {
            i.previous();
            fireTableRowsDeleted(startRow, viewRow);
        }
        return i;
    }

    private void insertToView(TableModelEvent ev, ListIterator<Integer> insertion) {
        int startRow = -1;
        int viewRow = insertion.nextIndex();
        for (int sourceIndex = ev.getFirstRow(); sourceIndex <= ev.getLastRow(); sourceIndex++) {
            if (transactionsModel.hasAccount(sourceIndex, account)) {
                insertion.add(sourceIndex);
                if (startRow == -1) {
                    startRow = viewRow;
                }
                viewRow++;
            }
        }
        if (startRow != -1) {
            fireTableRowsInserted(startRow, viewRow - 1);
        }
    }
}
