package org.dukecash.views;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import javafx.scene.control.TreeItem;
import org.dukecash.models.Account;

public class AccountTreeItemList extends TransformationList<TreeItem<Account>, Account> {
    private final ObservableList<Account> accounts;

    public AccountTreeItemList(ObservableList<Account> accounts) {
        super(accounts);
        this.accounts = accounts;
    }

    @Override
    public TreeItem<Account> set(int index, TreeItem<Account> element) {
        return AccountTreeItem.reference(this.accounts.set(index, element.getValue()));
    }

    @Override
    public void add(int index, TreeItem<Account> element) {
        this.accounts.add(index, element.getValue());
    }

    @Override
    public TreeItem<Account> remove(int index) {
        return AccountTreeItem.reference(this.accounts.remove(index));
    }

    @Override
    public int size() {
        return getSource().size();
    }

    @Override
    public AccountTreeItem get(int index) {
        return new AccountTreeItem(getSource().get(index));
    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends Account> c) {
    }

    @Override
    public int getSourceIndex(int index) {
        return index;
    }

    @Override
    public int getViewIndex(int index) {
        return index;
    }
}
