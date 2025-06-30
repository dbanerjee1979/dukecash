package org.dukecash;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DukeCash extends Application {
    @Override
    public void start(Stage stage) {
        Config config = new Config();

        BorderPane borderPane = new BorderPane();

        Scene scene = new Scene(borderPane, 1024, 768);
        stage.setTitle("DukeCash");
        stage.setScene(scene);

        stage.setMaximized(config.maximized.get());
        stage.setWidth(config.width.get());
        stage.setHeight(config.height.get());

        config.maximized.bind(stage.maximizedProperty());
        config.width.bind(stage.widthProperty());
        config.height.bind(stage.heightProperty());

        stage.setOnCloseRequest(ev -> config.save());

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
