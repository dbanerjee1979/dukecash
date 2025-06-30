package org.dukecash.views;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.dukecash.models.Account;
import org.dukecash.models.AccountType;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Optional;

public class AccountEditor extends Dialog<Account> {
    public AccountEditor(Account account, AccountTreeItem root, AccountTreeItem parent) {
        TabPane tabPane = new TabPane();

        Tab generalTab = new Tab("General", createGeneralPane(account, root, parent));
        generalTab.setClosable(false);
        Tab balanceTab = new Tab("Opening Balance", createBalancePane());
        balanceTab.setClosable(false);
        tabPane.getTabs().addAll(generalTab, balanceTab);

        setTitle("New Account");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(tabPane);
        setResizable(true);
    }


    private BorderPane createGeneralPane(Account account, AccountTreeItem root, AccountTreeItem parent) {
        Label accountNameLabel = new Label("Account Name");
        TextField accountNameField = new TextField();
        account.name.bind(accountNameField.textProperty());

        Label descriptionLabel = new Label("Description");
        TextField descriptionField = new TextField();
        account.description.bind(descriptionField.textProperty());

        Label parentAccountLabel = new Label("Parent Account");
        TreeView<Account> parentAccountPicker = new TreeView<>();
        parentAccountPicker.setRoot(root);
        MultipleSelectionModel<TreeItem<Account>> selectionModel = parentAccountPicker.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        ReadOnlyObjectProperty<TreeItem<Account>> parentAccount = selectionModel.selectedItemProperty();
        account.parent.bind(Bindings.createObjectBinding(
                () -> Optional.ofNullable(parentAccount.get()).map(TreeItem::getValue).orElse(null),
                parentAccount));
        selectionModel.select(parent);

        Label accountTypeLabel = new Label("Account Type");
        ComboBox<AccountType> accountTypePicker = new ComboBox<>(FXCollections.observableArrayList(
                EnumSet.complementOf(EnumSet.of(AccountType.Root))));
        account.accountType.bind(accountTypePicker.valueProperty());

        CheckBox placeholderAccount = new CheckBox("Placeholder Account");
        account.placeholder.bind(placeholderAccount.selectedProperty());

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        GridPane.setMargin(gridPane, new Insets(8, 8, 8, 8));

        gridPane.addRow(0, accountNameLabel, accountNameField);
        GridPane.setHalignment(accountNameLabel, HPos.RIGHT);
        GridPane.setHgrow(accountNameField, Priority.ALWAYS);

        gridPane.addRow(1, descriptionLabel, descriptionField);
        GridPane.setHalignment(descriptionLabel, HPos.RIGHT);
        GridPane.setHgrow(descriptionField, Priority.ALWAYS);

        gridPane.addRow(2, parentAccountLabel, parentAccountPicker);
        GridPane.setHalignment(parentAccountLabel, HPos.RIGHT);
        GridPane.setValignment(parentAccountLabel, VPos.BASELINE);
        GridPane.setHgrow(parentAccountPicker, Priority.ALWAYS);
        GridPane.setVgrow(parentAccountPicker, Priority.ALWAYS);

        gridPane.addRow(3, accountTypeLabel, accountTypePicker);
        GridPane.setHalignment(accountTypeLabel, HPos.RIGHT);

        gridPane.add(placeholderAccount, 1, 4);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(gridPane);
        BorderPane.setMargin(gridPane, new Insets(8, 8, 8, 8));

        return borderPane;
    }

    private BorderPane createBalancePane() {
        Label balanceLabel = new Label("Balance");
        Spinner<BigDecimal> balanceField = new Spinner<>();

        Label dateLabel = new Label("Date");
        DatePicker dateField = new DatePicker();

        Label transferAccountLabel = new Label("Transfer Account");
        TreeView<Account> transferAccountPicker = new TreeView<>();

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        GridPane.setMargin(gridPane, new Insets(8, 8, 8, 8));

        gridPane.addRow(0, balanceLabel, balanceField);
        GridPane.setHalignment(balanceLabel, HPos.RIGHT);
        GridPane.setHgrow(balanceField, Priority.ALWAYS);

        gridPane.addRow(1, dateLabel, dateField);
        GridPane.setHalignment(dateLabel, HPos.RIGHT);
        GridPane.setHgrow(dateField, Priority.ALWAYS);

        gridPane.addRow(2, transferAccountLabel, transferAccountPicker);
        GridPane.setHalignment(transferAccountLabel, HPos.RIGHT);
        GridPane.setValignment(transferAccountLabel, VPos.BASELINE);
        GridPane.setHgrow(transferAccountPicker, Priority.ALWAYS);
        GridPane.setVgrow(transferAccountPicker, Priority.ALWAYS);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(gridPane);
        BorderPane.setMargin(gridPane, new Insets(8, 8, 8, 8));
        return borderPane;
    }
}
