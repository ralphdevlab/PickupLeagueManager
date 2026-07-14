package com.pickupleague;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.DateTimeException;
import java.util.List;

/**
 * The graphical user interface (View layer) for the Pickup League Manager.
 *
 * Shows every player in a table, with a form below for adding and editing.
 * All CRUD operations and the custom action are available as buttons.
 * The GUI never touches the data directly: it calls methods on PlayerList
 * and displays whatever those methods return, keeping the MVC separation.
 *
 * All user input is validated before it reaches the logic layer, and any
 * error is shown as an inline red message rather than crashing the program.
 */
public class PlayerGUI {

    // --- Colour palette (kept in one place so the look stays consistent) ---
    private final Color TEAL_DARK   = new Color(15, 95, 92);
    private final Color TEAL_LIGHT  = new Color(224, 240, 239);
    private final Color BG_GREY     = new Color(245, 245, 242);
    private final Color WHITE       = Color.WHITE;
    private final Color TEXT_DARK   = new Color(35, 42, 42);
    private final Color TEXT_MUTED  = new Color(120, 122, 118);
    private final Color GREEN_BG    = new Color(222, 241, 231);
    private final Color GREEN_TX    = new Color(20, 90, 65);
    private final Color RED_BG      = new Color(250, 231, 224);
    private final Color RED_TX      = new Color(150, 55, 30);
    private final Color ERROR_RED   = new Color(180, 45, 45);

    private final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD, 20);
    private final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 12);
    private final Font FONT_LABEL   = new Font("SansSerif", Font.PLAIN, 12);
    private final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    private final Font FONT_BTN     = new Font("SansSerif", Font.BOLD, 12);

    // --- The data layer this GUI talks to ---
    private final PlayerList playerList;

    // --- Components the GUI needs to reach later ---
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField idField, firstField, lastField, dobField, jerseyField, teamField;
    private JComboBox<String> sportBox;
    private JLabel errorLabel;
    private JLabel statusLabel;

    public PlayerGUI(PlayerList playerList) {
        this.playerList = playerList;
    }

    /**
     * Builds and shows the window. Returns true once the interface is on screen,
     * so the launcher can confirm startup succeeded.
     */
    public boolean launch() {
        frame = new JFrame("Pickup League Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(980, 720);
        frame.setLocationRelativeTo(null);          // centre on screen
        frame.getContentPane().setBackground(BG_GREY);
        frame.setLayout(new BorderLayout());

        frame.add(buildHeader(), BorderLayout.NORTH);
        frame.add(buildCentre(), BorderLayout.CENTER);
        frame.add(buildStatusBar(), BorderLayout.SOUTH);

        refreshTable();
        frame.setVisible(true);
        return true;
    }

    // ---------- header bar ----------

    /** Builds the coloured title bar across the top of the window. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(TEAL_DARK);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Pickup League Manager");
        title.setFont(FONT_TITLE);
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel("Soccer and basketball roster management");
        subtitle.setFont(FONT_SUB);
        subtitle.setForeground(new Color(159, 214, 210));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    // ---------- centre: table + form ----------

    private JPanel buildCentre() {
        JPanel centre = new JPanel(new BorderLayout(0, 12));
        centre.setBackground(BG_GREY);
        centre.setBorder(new EmptyBorder(16, 16, 8, 16));
        centre.add(buildTablePanel(), BorderLayout.CENTER);
        centre.add(buildFormPanel(), BorderLayout.SOUTH);
        return centre;
    }

    /** Builds the white card holding the player table and the action buttons. */
    private JPanel buildTablePanel() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 214)),
                new EmptyBorder(14, 14, 14, 14)));

        JLabel heading = new JLabel("Players");
        heading.setFont(new Font("SansSerif", Font.BOLD, 15));
        heading.setForeground(TEXT_DARK);

        String[] columns = {"ID", "Name", "Sport", "Jersey", "Team", "Age", "Eligibility"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // the table is for viewing; editing happens in the form
            }
        };

        table = new JTable(tableModel);
        table.setFont(FONT_BODY);
        table.setRowHeight(28);
        table.setSelectionBackground(TEAL_LIGHT);
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(new Color(235, 235, 230));
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("SansSerif", Font.BOLD, 12));
        th.setBackground(TEAL_DARK);
        th.setForeground(WHITE);
        th.setReorderingAllowed(false);


        // Colour the eligibility column green (eligible) or red (ineligible)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                                                           boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                String text = String.valueOf(value);
                setOpaque(true);
                if (text.startsWith("Eligible")) {
                    setBackground(sel ? TEAL_LIGHT : GREEN_BG);
                    setForeground(GREEN_TX);
                } else {
                    setBackground(sel ? TEAL_LIGHT : RED_BG);
                    setForeground(RED_TX);
                }
                setFont(new Font("SansSerif", Font.BOLD, 12));
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        // When a row is clicked, copy that player's values into the form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelectedRow();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 225)));
        scroll.getViewport().setBackground(WHITE);

        card.add(heading, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(buildButtonBar(), BorderLayout.SOUTH);
        return card;
    }

    /** Builds the row of action buttons under the table. */
    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(WHITE);

        JButton loadBtn   = makeButton("Load from file", TEAL_DARK, WHITE);
        JButton addBtn    = makeButton("Add player", TEAL_DARK, WHITE);
        JButton updateBtn = makeButton("Update selected", new Color(60, 52, 137), WHITE);
        JButton deleteBtn = makeButton("Delete selected", new Color(153, 60, 29), WHITE);
        JButton avgBtn    = makeButton("Average age of team", new Color(133, 79, 11), WHITE);
        JButton clearBtn  = makeButton("Clear form", new Color(235, 235, 230), TEXT_DARK);

        loadBtn.addActionListener(e -> doLoadFile());
        addBtn.addActionListener(e -> doAddPlayer());
        updateBtn.addActionListener(e -> doUpdatePlayer());
        deleteBtn.addActionListener(e -> doDeletePlayer());
        avgBtn.addActionListener(e -> doAverageAge());
        clearBtn.addActionListener(e -> clearForm());

        bar.add(loadBtn);
        bar.add(addBtn);
        bar.add(updateBtn);
        bar.add(deleteBtn);
        bar.add(avgBtn);
        bar.add(clearBtn);
        return bar;
    }

    /** Creates a flat, coloured button. Returns the finished button. */
    private JButton makeButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(FONT_BTN);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Builds the white card holding the add / edit form fields. */
    private JPanel buildFormPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 214)),
                new EmptyBorder(14, 14, 14, 14)));

        JLabel heading = new JLabel("Player details");
        heading.setFont(new Font("SansSerif", Font.BOLD, 15));
        heading.setForeground(TEXT_DARK);

        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 8));
        grid.setBackground(WHITE);

        idField     = new JTextField();
        firstField  = new JTextField();
        lastField   = new JTextField();
        dobField    = new JTextField();
        jerseyField = new JTextField();
        teamField   = new JTextField();
        sportBox    = new JComboBox<>(new String[] {"Soccer", "Basketball"});
        sportBox.setFont(FONT_BODY);
        sportBox.setBackground(WHITE);

        grid.add(labelled("Player ID (1-9999)", idField));
        grid.add(labelled("First name", firstField));
        grid.add(labelled("Last name", lastField));
        grid.add(labelled("Date of birth (yyyy-MM-dd)", dobField));
        grid.add(labelled("Sport", sportBox));
        grid.add(labelled("Jersey number (0-99)", jerseyField));
        grid.add(labelled("Team ID (1-999)", teamField));
        grid.add(new JLabel());   // spacer to balance the grid

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        errorLabel.setForeground(ERROR_RED);

        card.add(heading, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(errorLabel, BorderLayout.SOUTH);
        return card;
    }

    /** Wraps a field with its caption above it. Returns the finished panel. */
    private JPanel labelled(String caption, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setBackground(WHITE);
        JLabel l = new JLabel(caption);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_MUTED);
        field.setFont(FONT_BODY);
        if (field instanceof JTextField) {
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(215, 215, 210)),
                    new EmptyBorder(6, 8, 6, 8)));
        }
        p.add(l, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    /** Builds the small status bar along the bottom of the window. */
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(TEAL_LIGHT);
        bar.setBorder(new EmptyBorder(8, 20, 8, 20));
        statusLabel = new JLabel("Ready. Load a file or add a player to begin.");
        statusLabel.setFont(FONT_SUB);
        statusLabel.setForeground(TEAL_DARK);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // ---------- actions ----------

    /** Opens a file chooser, loads the chosen file, and reports the result. */
    private boolean doLoadFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a player data file");
        int choice = chooser.showOpenDialog(frame);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return false;   // the user cancelled; nothing to do
        }
        File file = chooser.getSelectedFile();
        String result = playerList.loadFromFile(file.getAbsolutePath());
        refreshTable();
        showStatus(result);
        return true;
    }

    /** Reads the form, validates it, and adds a new player. Returns true on success. */
    private boolean doAddPlayer() {
        clearError();
        Player player = buildPlayerFromForm(true);
        if (player == null) {
            return false;   // buildPlayerFromForm already showed the error
        }
        String result = playerList.addPlayer(player);
        if (result.startsWith("Could not add")) {
            showError(result);
            return false;
        }
        refreshTable();
        clearForm();
        showStatus(result);
        return true;
    }

    /** Updates every field of the selected player from the form. Returns true on success. */
    private boolean doUpdatePlayer() {
        clearError();
        Integer selectedId = getSelectedPlayerId();
        if (selectedId == null) {
            showError("Select a player in the table first.");
            return false;
        }
        Player edited = buildPlayerFromForm(false);
        if (edited == null) {
            return false;
        }
        // Apply each field through the logic layer, which re-validates and rolls back on error
        String result = playerList.updatePlayerField(selectedId, 1, edited.getFirstName());
        if (result.startsWith("Update rejected") || result.startsWith("Update failed")) {
            showError(result); return false;
        }
        result = playerList.updatePlayerField(selectedId, 2, edited.getLastName());
        if (result.startsWith("Update rejected") || result.startsWith("Update failed")) {
            showError(result); return false;
        }
        result = playerList.updatePlayerField(selectedId, 3, edited.getDateOfBirth().toString());
        if (result.startsWith("Update rejected") || result.startsWith("Update failed")) {
            showError(result); return false;
        }
        result = playerList.updatePlayerField(selectedId, 4, edited.getSport());
        if (result.startsWith("Update rejected") || result.startsWith("Update failed")) {
            showError(result); return false;
        }
        result = playerList.updatePlayerField(selectedId, 6, String.valueOf(edited.getTeamId()));
        if (result.startsWith("Update rejected") || result.startsWith("Update failed")) {
            showError(result); return false;
        }
        result = playerList.updatePlayerField(selectedId, 5, String.valueOf(edited.getJerseyNumber()));
        if (result.startsWith("Update rejected") || result.startsWith("Update failed")) {
            showError(result); return false;
        }
        refreshTable();
        showStatus("Player ID " + selectedId + " updated successfully.");
        return true;
    }

    /** Deletes the selected player after asking for confirmation. Returns true if removed. */
    private boolean doDeletePlayer() {
        clearError();
        Integer selectedId = getSelectedPlayerId();
        if (selectedId == null) {
            showError("Select a player in the table first.");
            return false;
        }
        int confirm = JOptionPane.showConfirmDialog(frame,
                "Remove player ID " + selectedId + "? This cannot be undone.",
                "Confirm deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return false;
        }
        String result = playerList.removePlayerById(selectedId);
        refreshTable();
        clearForm();
        showStatus(result);
        return true;
    }

    /** Custom action: asks for a team ID and shows that team's average age. */
    private boolean doAverageAge() {
        clearError();
        String input = JOptionPane.showInputDialog(frame,
                "Enter a Team ID (1-999) to calculate its average age:",
                "Custom action: average age", JOptionPane.QUESTION_MESSAGE);
        if (input == null) {
            return false;   // cancelled
        }
        int teamId;
        try {
            teamId = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            showError("Team ID must be a whole number.");
            return false;
        }
        if (teamId < 1 || teamId > 999) {
            showError("Team ID must be between 1 and 999.");
            return false;
        }
        double avg = playerList.calculateAverageAgeForTeam(teamId);
        if (avg < 0) {
            JOptionPane.showMessageDialog(frame,
                    "No players are on team " + teamId + ".",
                    "Average age", JOptionPane.INFORMATION_MESSAGE);
            showStatus("Team " + teamId + " has no players.");
            return false;
        }
        int count = playerList.countPlayersOnTeam(teamId);
        String message = String.format("Team %d has %d player(s).%nAverage age: %.1f years.",
                teamId, count, avg);
        JOptionPane.showMessageDialog(frame, message, "Average age",
                JOptionPane.INFORMATION_MESSAGE);
        showStatus(String.format("Team %d average age: %.1f years (%d players).", teamId, avg, count));
        return true;
    }

    // ---------- form helpers ----------

    /**
     * Reads every form field, validating each one, and returns a Player.
     * Returns null (after showing an inline red error) if anything is wrong,
     * so bad data never reaches the logic layer and the program cannot crash.
     */
    private Player buildPlayerFromForm(boolean checkDuplicateId) {
        // Player ID
        int id;
        try {
            id = Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Player ID must be a whole number.");
            return null;
        }
        if (id < 1 || id > 9999) {
            showError("Player ID must be between 1 and 9999.");
            return null;
        }
        if (checkDuplicateId && playerList.idExists(id)) {
            showError("A player with ID " + id + " already exists.");
            return null;
        }

        // Names
        String first = firstField.getText().trim();
        String last = lastField.getText().trim();
        if (!isNameOk(first)) {
            showError("First name must be letters and spaces only, and cannot be empty.");
            return null;
        }
        if (!isNameOk(last)) {
            showError("Last name must be letters and spaces only, and cannot be empty.");
            return null;
        }

        // Date of birth
        LocalDate dob = parseDob(dobField.getText().trim());
        if (dob == null) {
            return null;   // parseDob already showed the specific problem
        }

        // Jersey number
        int jersey;
        try {
            jersey = Integer.parseInt(jerseyField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Jersey number must be a whole number.");
            return null;
        }
        if (jersey < 0 || jersey > 99) {
            showError("Jersey number must be between 0 and 99.");
            return null;
        }

        // Team ID
        int teamId;
        try {
            teamId = Integer.parseInt(teamField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Team ID must be a whole number.");
            return null;
        }
        if (teamId < 1 || teamId > 999) {
            showError("Team ID must be between 1 and 999.");
            return null;
        }

        String sport = String.valueOf(sportBox.getSelectedItem());
        return new Player(id, first, last, dob, sport, jersey, teamId, "Pending");
    }

    /** Returns true if a name is letters and spaces only, non-empty, not too long. */
    private boolean isNameOk(String name) {
        return name != null && !name.isEmpty() && name.length() <= 40
                && name.matches("[a-zA-Z][a-zA-Z ]*");
    }

    /**
     * Parses a date and reports EXACTLY what is wrong if it fails
     * (bad format, impossible month, impossible day, unreasonable year).
     * Returns the date, or null if the text could not be accepted.
     */
    private LocalDate parseDob(String raw) {
        if (!raw.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showError("Date must be in the format yyyy-MM-dd, e.g. 2001-08-15.");
            return null;
        }
        String[] parts = raw.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);
        int thisYear = LocalDate.now().getYear();
        if (year < 1900 || year > thisYear) {
            showError("Year " + year + " is not valid. Use a year between 1900 and " + thisYear + ".");
            return null;
        }
        if (month < 1 || month > 12) {
            showError("Month " + month + " does not exist. Months are 01 to 12.");
            return null;
        }
        if (day < 1 || day > 31) {
            showError("Day " + day + " does not exist. Days are 01 to 31.");
            return null;
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            showError("That day does not exist in that month (for example, February has no 30th).");
            return null;
        }
    }

    /** Copies the selected table row's player into the form fields. */
    private boolean fillFormFromSelectedRow() {
        Integer id = getSelectedPlayerId();
        if (id == null) {
            return false;
        }
        Player p = playerList.findById(id);
        if (p == null) {
            return false;
        }
        idField.setText(String.valueOf(p.getPlayerId()));
        firstField.setText(p.getFirstName());
        lastField.setText(p.getLastName());
        dobField.setText(p.getDateOfBirth().toString());
        sportBox.setSelectedItem(p.getSport());
        jerseyField.setText(String.valueOf(p.getJerseyNumber()));
        teamField.setText(String.valueOf(p.getTeamId()));
        return true;
    }

    /** Returns the player ID of the selected table row, or null if nothing is selected. */
    private Integer getSelectedPlayerId() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (Integer) tableModel.getValueAt(row, 0);
    }

    /** Empties every form field and clears any error message. */
    private boolean clearForm() {
        idField.setText("");
        firstField.setText("");
        lastField.setText("");
        dobField.setText("");
        jerseyField.setText("");
        teamField.setText("");
        sportBox.setSelectedIndex(0);
        table.clearSelection();
        clearError();
        return true;
    }

    // ---------- display helpers ----------

    /** Rebuilds the table from the current data so the screen always matches the list. */
    private boolean refreshTable() {
        tableModel.setRowCount(0);
        List<Player> all = playerList.getAllPlayers();
        for (Player p : all) {
            tableModel.addRow(new Object[] {
                    p.getPlayerId(),
                    p.getFirstName() + " " + p.getLastName(),
                    p.getSport(),
                    p.getJerseyNumber(),
                    p.getTeamId(),
                    p.calculateAge(),
                    p.getEligibilityStatus()
            });
        }
        return true;
    }

    /** Shows a red inline error under the form. */
    private boolean showError(String message) {
        errorLabel.setText("\u26A0  " + message);
        return true;
    }

    /** Clears the inline error message. */
    private boolean clearError() {
        errorLabel.setText(" ");
        return true;
    }

    /** Writes a message into the status bar at the bottom of the window. */
    private boolean showStatus(String message) {
        statusLabel.setText(message + "   |   " + playerList.size() + " player(s) in the system.");
        return true;
    }
}