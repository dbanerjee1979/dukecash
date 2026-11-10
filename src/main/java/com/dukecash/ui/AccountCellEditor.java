package com.dukecash.ui;

import com.dukecash.model.Account;
import com.dukecash.model.AccountsTreeTableModel;

import javax.swing.*;
import java.awt.*;

import static java.util.function.Predicate.not;

class AccountCellEditor extends DefaultCellEditor {
    private final Account fromAccount;
    private final AccountsTreeTableModel accountsModel;

    public AccountCellEditor(Account fromAccount, AccountsTreeTableModel accountsModel) {
        super(new JComboBox<>());
        this.fromAccount = fromAccount;
        this.accountsModel = accountsModel;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        @SuppressWarnings("unchecked")
        JComboBox<Account> comboBox = (JComboBox<Account>) this.editorComponent;
        comboBox.removeAllItems();
        accountsModel.findAllAccounts().stream()
                .filter(not(fromAccount::equals))
                .forEach(comboBox::addItem);
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
