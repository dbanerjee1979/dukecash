package org.dukecash.views;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.StackPane;
import org.dukecash.models.Account;

public class AccountListView extends StackPane {
    public AccountListView(AccountTreeItem root, ObjectProperty<AccountTreeItem> selectedAccount) {
        TreeTableColumn<Account, String> accountNameColumn = new TreeTableColumn<>("Account Name");
        accountNameColumn.setPrefWidth(250);
        accountNameColumn.cellValueFactoryProperty().setValue(p -> p.getValue().getValue().name);

        TreeTableColumn<Account, String> descriptionColumn = new TreeTableColumn<>("Description");
        descriptionColumn.setPrefWidth(600);

        TreeTableColumn<Account, Number> totalColumn = new TreeTableColumn<>("Total");
        totalColumn.setPrefWidth(75);

        TreeTableView<Account> accountsTable = new TreeTableView<>();
        ObservableList<TreeTableColumn<Account, ?>> columns = accountsTable.getColumns();
        columns.add(accountNameColumn);
        columns.add(descriptionColumn);
        columns.add(totalColumn);
        accountsTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        root.setExpanded(true);
        accountsTable.setRoot(root);
        accountsTable.setShowRoot(true);
        ReadOnlyObjectProperty<TreeItem<Account>> selectedItem = accountsTable.getSelectionModel().selectedItemProperty();
        selectedAccount.bind(Bindings.createObjectBinding(
                () -> (AccountTreeItem) selectedItem.get(),
                selectedItem));

        getChildren().add(accountsTable);
    }
}
