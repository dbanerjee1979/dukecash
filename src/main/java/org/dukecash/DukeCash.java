package org.dukecash;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.dukecash.models.Account;
import org.dukecash.models.AccountType;
import org.dukecash.views.AccountEditor;
import org.dukecash.views.AccountListView;
import org.dukecash.views.AccountTreeItem;

import java.io.File;
import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

public class DukeCash extends Application {
    private final SimpleBooleanProperty changesPending = new SimpleBooleanProperty();

    private Config config;
    private ObservableList<Tab> tabs;
    private ObservableBooleanValue selectedAccountList;
    private SimpleObjectProperty<AccountTreeItem> selectedAccount = new SimpleObjectProperty<>();

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        this.config = new Config();

        Account rootAccount = new Account(AccountType.Root, "Accounts");

        TabPane tabPane = new TabPane();
        tabs = tabPane.getTabs();
        ReadOnlyObjectProperty<Tab> selectedTab = tabPane.getSelectionModel().selectedItemProperty();
        selectedAccountList = Bindings.createBooleanBinding(
                () -> Optional.ofNullable(selectedTab.get())
                        .map(Tab::getContent)
                        .map(AccountListView.class::isInstance)
                        .orElse(false),
                selectedTab);

        AccountTreeItem rootItem = new AccountTreeItem(rootAccount);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(createMenubar(stage, rootItem));
        borderPane.setCenter(tabPane);

        Scene scene = new Scene(borderPane, 1024, 768);
        stage.setTitle("DukeCash");
        stage.setScene(scene);

        stage.setMaximized(config.maximized.get());
        stage.setWidth(config.width.get());
        stage.setHeight(config.height.get());

        config.maximized.bind(stage.maximizedProperty());
        config.width.bind(stage.widthProperty());
        config.height.bind(stage.heightProperty());

        stage.setOnCloseRequest(ev -> quit(stage, ev));

        stage.show();
    }

    private void quit(Stage stage, WindowEvent ev) {
        if (!proceedWithUnsavedChanges()) {
            if (ev != null) {
                ev.consume();
            }
            return;
        }
        config.save();
        if (ev == null) {
            stage.close();
        }
    }

    private void newFile(AccountTreeItem root) {
        if (proceedWithUnsavedChanges()) {
            tabs.clear();
            Tab accountTab = new Tab("Accounts");
            accountTab.setClosable(false);
            accountTab.setContent(new AccountListView(root, selectedAccount));
            tabs.add(accountTab);
        }
    }

    private void openFile(Stage stage) {
        if (proceedWithUnsavedChanges()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("DukeCash Files", "*.dukecash"));
            File selectedFile = fileChooser.showOpenDialog(stage);
            System.out.println(selectedFile);
        }
    }

    private boolean proceedWithUnsavedChanges() {
        return !changesPending.get() ||
                new Alert(
                        CONFIRMATION,
                        "There are unsaved changes. Proceed?",
                        ButtonType.YES, ButtonType.NO)
                    .showAndWait()
                    .map(result -> result == ButtonType.YES)
                    .orElse(false);
    }

    private MenuBar createMenubar(Stage stage, AccountTreeItem root) {
        MenuBar menubar = new MenuBar();
        ObservableList<Menu> menus = menubar.getMenus();

        ObservableList<MenuItem> items;

        MenuItem newFileMenuItem = new MenuItem("_New File");
        newFileMenuItem.setMnemonicParsing(true);
        newFileMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newFileMenuItem.setOnAction(ev -> this.newFile(root));

        MenuItem openFileMenuItem = new MenuItem("_Open File");
        openFileMenuItem.setMnemonicParsing(true);
        openFileMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        openFileMenuItem.setOnAction(ev -> this.openFile(stage));

        MenuItem saveFileMenuItem = new MenuItem("_Save File");
        saveFileMenuItem.setMnemonicParsing(true);
        saveFileMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveFileMenuItem.disableProperty().bind(Bindings.not(changesPending));

        MenuItem quitMenuItem = new MenuItem("_Quit");
        quitMenuItem.setMnemonicParsing(true);
        quitMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        quitMenuItem.setOnAction(ev -> this.quit(stage, null));

        Menu fileMenu = new Menu("_File");
        fileMenu.setMnemonicParsing(true);
        items = fileMenu.getItems();
        items.add(newFileMenuItem);
        items.add(openFileMenuItem);
        items.add(saveFileMenuItem);
        items.add(new SeparatorMenuItem());
        items.add(quitMenuItem);

        MenuItem newAccountMenuItem = new MenuItem("New _Account");
        newAccountMenuItem.setMnemonicParsing(true);
        newAccountMenuItem.setOnAction(ev -> this.newAccount(stage, root));
        newAccountMenuItem.disableProperty().bind(Bindings.not(selectedAccountList));

        Menu accountsMenu = new Menu("_Accounts");
        accountsMenu.setMnemonicParsing(true);
        items = accountsMenu.getItems();
        items.add(newAccountMenuItem);

        menus.add(fileMenu);
        menus.add(accountsMenu);

        return menubar;
    }

    private void newAccount(Stage stage, AccountTreeItem root) {
        Account account = new Account();
        AccountEditor dialog = new AccountEditor(account, root, selectedAccount.get());
        dialog.showAndWait();
        root.find(account.parent.get()).getChildren().add(new AccountTreeItem(account));
    }
}
