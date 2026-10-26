package com.dukecash.ui;

import javax.swing.*;
import java.awt.*;

public class DukeCashApp extends JFrame {
    public DukeCashApp() {
        super("DukeCash");

        setSize(1024, 768);
    }

    static void main() {
        System.setProperty("swing.metalTheme", "steel");
        SwingUtilities.invokeLater(DukeCashApp::run);
    }

    private static void run() {
        DukeCashApp app = new DukeCashApp();
        app.setDefaultCloseOperation(EXIT_ON_CLOSE);
        app.setVisible(true);
    }
}
