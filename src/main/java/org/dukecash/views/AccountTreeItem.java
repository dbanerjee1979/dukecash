package org.dukecash.views;

import javafx.beans.binding.Bindings;
import javafx.scene.control.TreeItem;
import org.dukecash.models.Account;

public class AccountTreeItem extends TreeItem<Account> {
    private final AccountTreeItemList children;

    public AccountTreeItem(Account account) {
        super(account);
        this.children = new AccountTreeItemList(account.children);
        Bindings.bindContentBidirectional(getChildren(), this.children);
    }

    public static TreeItem<Account> reference(Account account) {
        return new TreeItem<>(account);
    }

    public AccountTreeItem find(Account account) {
        if (getValue() == account) {
            return this;
        }
        for (TreeItem<Account> node : getChildren()) {
            AccountTreeItem child = ((AccountTreeItem) node).find(account);
            if (child != null) {
                return child;
            }
        }
        return null;
    }
}
