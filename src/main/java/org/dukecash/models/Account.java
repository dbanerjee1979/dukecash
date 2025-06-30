package org.dukecash.models;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;

public class Account {
    public final SimpleObjectProperty<AccountType> accountType = new SimpleObjectProperty<>();
    public final SimpleStringProperty name = new SimpleStringProperty();
    public final SimpleStringProperty description = new SimpleStringProperty();
    public final ObservableList<Account> children = FXCollections.observableArrayList();
    public final SimpleObjectProperty<Account> parent = new SimpleObjectProperty<>();
    public final SimpleBooleanProperty placeholder = new SimpleBooleanProperty();

    public Account() {
    }

    @Override
    public String toString() {
        return this.name.getValue();
    }

    public Account(AccountType accountType, String name, Account... accounts) {
        this.accountType.set(accountType);
        this.name.set(name);
        children.addAll(Arrays.asList(accounts));
    }
}
