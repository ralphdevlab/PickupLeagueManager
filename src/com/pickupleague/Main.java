package com.pickupleague;

import javax.swing.SwingUtilities;

/**
 * Entry point for the Pickup League Manager application.
 * <p>
 * On startup this class asks the user for their database connection details
 * through a {@link ConnectionDialog}, and only opens the main
 * {@link PlayerGUI} window once a working connection has been established. If
 * the user cancels the connection screen the program exits quietly.
 * <p>
 * The {@code main} method is static because Java requires it to be, but it does
 * almost nothing: it creates a Main object and immediately hands off to the
 * instance method {@link #startApplication()}, so all real logic lives in
 * non-static code.
 *
 * @author Ralph Alexandre
 * @version 4.0
 */
public class Main {

    /**
     * Creates a new Main. No setup is required at construction time.
     */
    public Main() {
    }

    /**
     * Starts the program. Creates a Main object and calls its instance method
     * so that no application logic is written in static code.
     *
     * @param args command line arguments; not used by this application
     */
    public static void main(String[] args) {
        Main app = new Main();
        app.startApplication();
    }

    /**
     * Prompts for database credentials and, once a connection succeeds, opens
     * the main window. The work is scheduled on the Swing event dispatch thread,
     * which is the supported way to create an interface in Swing.
     *
     * @return true once the startup sequence has been scheduled
     */
    public boolean startApplication() {
        SwingUtilities.invokeLater(() -> {
            ConnectionDialog dialog = new ConnectionDialog();
            PlayerList playerList = dialog.promptForConnection();
            if (playerList == null) {
                System.exit(0);
                return;
            }
            PlayerGUI gui = new PlayerGUI(playerList);
            gui.launch();
        });
        return true;
    }
}