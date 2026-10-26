package com.dukecash.ui;

import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import java.awt.*;

import static java.awt.BorderLayout.CENTER;

public class DukeCashApp extends JFrame {
    public DukeCashApp() {
        super("DukeCash");

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Accounts", new AccountsPanel());

        add(tabs, CENTER);

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
