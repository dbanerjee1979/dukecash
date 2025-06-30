package org.dukecash;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

public class DukeCash extends Application {
    private final SimpleBooleanProperty changesPending = new SimpleBooleanProperty();

    private Config config;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        this.config = new Config();

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(createMenubar(stage));

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

    private void open(Stage stage) {
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

    private MenuBar createMenubar(Stage stage) {
        MenuBar menubar = new MenuBar();
        ObservableList<Menu> menus = menubar.getMenus();

        ObservableList<MenuItem> items;

        MenuItem newFileMenuItem = new MenuItem("_New File");
        newFileMenuItem.setMnemonicParsing(true);
        newFileMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));

        MenuItem openFileMenuItem = new MenuItem("_Open File");
        openFileMenuItem.setMnemonicParsing(true);
        openFileMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        openFileMenuItem.setOnAction(ev -> this.open(stage));

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

        menus.add(fileMenu);

        return menubar;
    }
}
