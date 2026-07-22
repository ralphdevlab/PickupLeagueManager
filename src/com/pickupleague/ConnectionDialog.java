package com.pickupleague;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;



/**
 * Collects the user's MySQL connection details at startup.
 * <p>
 * This dialog is shown before the main window opens. It asks for the server
 * address, username, and password, then builds a {@link PlayerList} from those
 * values and tests it. Nothing is hardcoded to one machine, so the application
 * runs on any computer that has the sample database installed.
 * <p>
 * If the details do not work the dialog explains why and asks again, so the
 * program never continues with a broken connection and never crashes on a bad
 * password.
 *
 * @author Ralph Alexandre
 * @version 4.0
 */
public class ConnectionDialog {

    /**
     * Creates a new ConnectionDialog. No setup is required at construction time.
     */
    private final Color TEAL_DARK = new Color(15, 95, 92);
    private final Color WHITE = Color.WHITE;

      /**
    * Repeatedly shows the connection form until the supplied details
    * successfully connect to the database, or the user cancels.
    * <p>
    * The server address and username must not be blank. Each attempt is
    * verified with {@link PlayerList#testConnection()} before the dialog
    * closes, so a returned PlayerList is always known to work.
    *
    *
    * @return a PlayerList already confirmed to connect, or null if the user
    *       canceled the dialog
    */

    public PlayerList promptForConnection() {
        while (true) {
            JTextField serverField = new JTextField("localhost:3306");
            JTextField userField = new JTextField("root");
            JPasswordField passField = new JPasswordField();

            JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
            panel.setBorder(new EmptyBorder(8, 8, 8, 8));
            panel.add(new JLabel("MySQL server address (host:port):"));
            panel.add(serverField);
            panel.add(new JLabel("Username:"));
            panel.add(userField);
            panel.add(new JLabel("Password:"));
            panel.add(passField);

            int choice = JOptionPane.showConfirmDialog(
                    null, panel, "Connect to Pickup League database",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (choice != JOptionPane.OK_OPTION) {
                return null; // user cancelled -> caller will exit
            }

            String server = serverField.getText().trim();
            String user = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (server.isEmpty() || user.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Server address and username cannot be empty.",
                        "Missing information", JOptionPane.WARNING_MESSAGE);
                continue;
            }

            PlayerList list = new PlayerList(server, user, password);
            String error = list.testConnection();
            if (error == null) {
                return list; // success!
            }

            // Connection failed: show why, then loop back to ask again
            int retry = JOptionPane.showConfirmDialog(null,
                    error + "\n\nTry again?",
                    "Connection failed", JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);
            if (retry != JOptionPane.YES_OPTION) {
                return null;
            }
        }
    }
}