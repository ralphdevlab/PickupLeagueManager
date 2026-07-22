package com.pickupleague;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores and retrieves {@link Player} records in a MySQL database.
 * <p>
 * This class is the data layer of the Pickup League Manager. It performs all
 * four CRUD operations and the custom action by running SQL against the
 * {@code players} table, and it validates every write through
 * {@link PlayerValidator} so invalid data can never reach the database.
 * <p>
 * The connection details are supplied by the user at run time and passed to the
 * constructor, so nothing is tied to one machine. The table and column names are
 * hardcoded, which is safe because every user runs the same sample database that
 * ships with the project.
 * <p>
 * Methods return descriptive result strings rather than throwing, so the
 * interface can report exactly what happened without the program ever crashing.
 *
 * @author Ralph Alexandre
 * @version 4.0
 */
public class PlayerList {

    /** The full JDBC connection string, built from the user's server address. */
    private final String url;

    /** The MySQL username supplied by the user. */
    private final String user;

    /** The MySQL password supplied by the user. */
    private final String password;

    /** Checks every player against the league's rules before a write. */
    private final PlayerValidator validator;

    /**
     * Creates a data layer that connects to the given MySQL server.
     * <p>
     * The connection string is assembled from the supplied server address, so
     * the program runs on any machine that has the sample database. Creating
     * this object does not open a connection; use {@link #testConnection()} to
     * confirm the details work.
     *
     * @param serverAddress the MySQL host and port, for example "localhost:3306"
     * @param user          the MySQL username
     * @param password      the MySQL password
     */
    public PlayerList(String serverAddress, String user, String password) {
        this.url = "jdbc:mysql://" + serverAddress + "/pickup_league";
        this.user = user;
        this.password = password;
        this.validator = new PlayerValidator(5, 60);
    }

    /**
     * Opens a fresh connection to the database. Each operation opens and closes
     * its own connection so none is held open longer than needed.
     *
     * @return an open database connection
     * @throws SQLException if the server cannot be reached or the credentials
     *                      are rejected
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Checks whether the stored connection details actually work, so the
     * connection screen can report a problem before the main window opens.
     *
     * @return null if the connection succeeded, otherwise a message explaining
     *         why it failed
     */
    public String testConnection() {
        try (Connection conn = getConnection()) {
            return null;
        } catch (SQLException e) {
            return "Could not connect: " + e.getMessage();
        }
    }

    /**
     * Validates a player and, if it passes every rule, inserts it into the
     * database. The player's eligibility status is calculated from their age
     * before the record is written.
     *
     * @param player the player to add
     * @return a message confirming the insert and the resulting eligibility, or
     *         a message explaining why the player was rejected
     */
    public String addPlayer(Player player) {
        String validationError = validator.getValidationError(player, getAllPlayers());
        if (validationError != null) {
            return "Could not add player: " + validationError;
        }
        player.setEligibilityStatus(player.determineEligibility(18, 45));

        String sql = "INSERT INTO players "
                + "(player_id, first_name, last_name, date_of_birth, sport, "
                + "jersey_number, team_id, eligibility_status) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, player.getPlayerId());
            ps.setString(2, player.getFirstName());
            ps.setString(3, player.getLastName());
            ps.setString(4, player.getDateOfBirth().toString());
            ps.setString(5, player.getSport());
            ps.setInt(6, player.getJerseyNumber());
            ps.setInt(7, player.getTeamId());
            ps.setString(8, player.getEligibilityStatus());
            ps.executeUpdate();
            return "Player added successfully (ID " + player.getPlayerId()
                    + ", " + player.getEligibilityStatus() + ").";
        } catch (SQLException e) {
            return "Database error while adding player: " + e.getMessage();
        }
    }

    /**
     * Reads every player from the database, ordered by player ID.
     * <p>
     * If the read fails the error is logged and an empty list is returned, so
     * callers never have to guard against a null result.
     *
     * @return every player currently stored, or an empty list if none could be
     *         read
     */
    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM players ORDER BY player_id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                players.add(rowToPlayer(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error while reading players: " + e.getMessage());
        }
        return players;
    }

    /**
     * Looks up a single player by their unique identifier.
     *
     * @param playerId the ID of the player to find
     * @return the matching player, or null if no player has that ID
     */
    public Player findById(int playerId) {
        String sql = "SELECT * FROM players WHERE player_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowToPlayer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while finding player: " + e.getMessage());
        }
        return null;
    }

    /**
     * Reports whether a player ID is already in use, so the interface can stop
     * the user creating a duplicate.
     *
     * @param playerId the ID to check
     * @return true if a player with that ID already exists
     */
    public boolean idExists(int playerId) {
        return findById(playerId) != null;
    }

    /**
     * Converts the current row of a result set into a Player object.
     *
     * @param rs a result set positioned on a row of the players table
     * @return a Player built from that row
     * @throws SQLException if a column cannot be read
     */
    private Player rowToPlayer(ResultSet rs) throws SQLException {
        return new Player(
                rs.getInt("player_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                LocalDate.parse(rs.getString("date_of_birth")),
                rs.getString("sport"),
                rs.getInt("jersey_number"),
                rs.getInt("team_id"),
                rs.getString("eligibility_status"));
    }

    /**
     * Deletes the player with the given ID from the database. Attempting to
     * delete a player who does not exist is reported to the user rather than
     * treated as an error.
     *
     * @param playerId the ID of the player to remove
     * @return a message naming the player who was removed, or explaining that
     *         no player with that ID was found
     */
    public String removePlayerById(int playerId) {
        Player target = findById(playerId);
        if (target == null) {
            return "No player found with ID " + playerId + ". Nothing was removed.";
        }
        String sql = "DELETE FROM players WHERE player_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.executeUpdate();
            return "Player ID " + playerId + " (" + target.getFirstName() + " "
                    + target.getLastName() + ") was removed.";
        } catch (SQLException e) {
            return "Database error while removing player: " + e.getMessage();
        }
    }

    /**
     * Changes one field of an existing player.
     * <p>
     * The change is applied to a copy of the record and re-validated before
     * anything is written, so an edit that would break a rule, such as taking a
     * jersey number already worn on that team, is rejected and the stored record
     * is left untouched. If the date of birth changes, the eligibility status is
     * recalculated to match.
     *
     * @param playerId    the ID of the player to change
     * @param fieldChoice which field to change: 1 first name, 2 last name,
     *                    3 date of birth, 4 sport, 5 jersey number, 6 team ID,
     *                    7 eligibility status
     * @param newValue    the new value as text; numbers and dates are parsed and
     *                    rejected cleanly if malformed
     * @return a message confirming the update, or explaining why it was rejected
     */
    public String updatePlayerField(int playerId, int fieldChoice, String newValue) {
        Player p = findById(playerId);
        if (p == null) {
            return "No player found with ID " + playerId + ".";
        }
        try {
            switch (fieldChoice) {
                case 1: p.setFirstName(newValue); break;
                case 2: p.setLastName(newValue); break;
                case 3: p.setDateOfBirth(LocalDate.parse(newValue.trim())); break;
                case 4: p.setSport(newValue); break;
                case 5: p.setJerseyNumber(Integer.parseInt(newValue.trim())); break;
                case 6: p.setTeamId(Integer.parseInt(newValue.trim())); break;
                case 7: p.setEligibilityStatus(newValue); break;
                default: return "Invalid field choice.";
            }
        } catch (NumberFormatException e) {
            return "Update failed: that field needs a whole number.";
        } catch (Exception e) {
            return "Update failed: date must be in the format yyyy-MM-dd.";
        }

        List<Player> others = getAllPlayers();
        String validationError = validator.getValidationError(p, others);
        if (validationError != null) {
            return "Update rejected: " + validationError + " No changes were made.";
        }

        p.setEligibilityStatus(p.determineEligibility(18, 45));

        String sql = "UPDATE players SET first_name=?, last_name=?, date_of_birth=?, "
                + "sport=?, jersey_number=?, team_id=?, eligibility_status=? WHERE player_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getDateOfBirth().toString());
            ps.setString(4, p.getSport());
            ps.setInt(5, p.getJerseyNumber());
            ps.setInt(6, p.getTeamId());
            ps.setString(7, p.getEligibilityStatus());
            ps.setInt(8, playerId);
            ps.executeUpdate();
            return "Player ID " + playerId + " updated successfully.";
        } catch (SQLException e) {
            return "Database error while updating player: " + e.getMessage();
        }
    }

    /**
     * The custom action of this project: calculates the mean age of every player
     * on a given team.
     * <p>
     * Each player's age is derived from their date of birth, so the result is
     * always current. An empty team returns -1 rather than dividing by zero,
     * letting the caller report that no players were found.
     *
     * @param teamId the team whose players should be averaged
     * @return the average age in years, or -1 if the team has no players
     */
    public double calculateAverageAgeForTeam(int teamId) {
        int total = 0;
        int count = 0;
        for (Player p : getAllPlayers()) {
            if (p.getTeamId() == teamId) {
                total += p.calculateAge();
                count++;
            }
        }
        if (count == 0) {
            return -1;
        }
        return (double) total / count;
    }

    /**
     * Counts how many players belong to a given team, used alongside the average
     * age calculation.
     *
     * @param teamId the team to count
     * @return the number of players on that team, which may be zero
     */
    public int countPlayersOnTeam(int teamId) {
        int count = 0;
        for (Player p : getAllPlayers()) {
            if (p.getTeamId() == teamId) {
                count++;
            }
        }
        return count;
    }

    /**
     * Reports how many players are stored in the database.
     *
     * @return the total number of player records
     */
    public int size() {
        return getAllPlayers().size();
    }
}