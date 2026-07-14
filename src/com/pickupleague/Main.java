package com.pickupleague;

import javax.swing.SwingUtilities;

/**
 * Application entry point for the Pickup League Manager (Phase 3, GUI).
 *
 * main() is static because Java requires it, but it does almost nothing:
 * it creates a Main object and calls the instance method startApplication(),
 * so all real logic lives in non-static methods.
 */
public class Main {

    public static void main(String[] args) {
        Main app = new Main();
        app.startApplication();
    }

    /**
     * Creates the data layer and the GUI, then shows the window.
     * Returns true once the application has started successfully.
     */
    public boolean startApplication() {
        SwingUtilities.invokeLater(() -> {
            PlayerList playerList = new PlayerList();
            PlayerGUI gui = new PlayerGUI(playerList);
            gui.launch();
        });
        return true;
    }
}