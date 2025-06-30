package org.dukecash;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@SuppressWarnings("CallToPrintStackTrace")
public class Config {
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String MAXIMIZED = "maximized";

    public final SimpleBooleanProperty maximized = new SimpleBooleanProperty();
    public final SimpleIntegerProperty width = new SimpleIntegerProperty();
    public final SimpleIntegerProperty height = new SimpleIntegerProperty();

    private final Properties config;
    private final Path settingsFile;

    public Config() {
        config = new Properties();
        config.setProperty(WIDTH, "1024");
        config.setProperty(HEIGHT, "768");
        config.setProperty(MAXIMIZED, "false");

        this.settingsFile = Path.of(System.getProperty("user.home"), ".dukecash", "app-settings.properties");
        createSettingsFile();

        maximized.setValue(Boolean.parseBoolean(config.getProperty(MAXIMIZED)));
        width.setValue(Integer.parseInt(config.getProperty(WIDTH)));
        height.setValue(Integer.parseInt(config.getProperty(HEIGHT)));

        maximized.addListener(updateConfig(MAXIMIZED));
        width.addListener(updateConfig(WIDTH));
        height.addListener(updateConfig(HEIGHT));
    }

    public void save() {
        createSettingsDir();
        try {
            try (OutputStream os = Files.newOutputStream(settingsFile)) {
                config.store(os, "User Session");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private void createSettingsDir() {
        try {
            if (!Files.exists(settingsFile.getParent())) {
                Files.createDirectory(settingsFile.getParent());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void createSettingsFile() {
        try {
            if (!Files.exists(settingsFile)) {
                save();
            }
            try (InputStream is = Files.newInputStream(settingsFile)) {
                config.load(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private <T> ChangeListener<? super T> updateConfig(String property) {
        return (src, oldValue, newValue) -> config.setProperty(property, newValue.toString());
    }
}
