package com.pickupleague;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the collection of Player objects and every operation on them:
 * the four CRUD operations, the custom action, and batch loading from a file.
 * Uses an in-memory ArrayList because Phase 1 has no database yet.
 * Methods return values (messages, booleans, objects) instead of being void.
 */
public class PlayerList {

    private final List<Player> players;   // the in-memory data store
    private final PlayerValidator validator;

    // Constructor: starts with an empty list and a validator (age range 16–45)
    public PlayerList() {
        this.players = new ArrayList<>();
        this.validator = new PlayerValidator(5, 60); // wide range: who can be recorded at all
    }

    // CREATE (manual)
    /**
     * Adds a fully built Player. Returns a message describing success,
     * or the specific reason the player was rejected.
     */
    public String addPlayer(Player player) {
        String validationError = validator.getValidationError(player, players);
        if (validationError != null) {
            return "Could not add player: " + validationError;
        }
        // Eligible only if age is 18 to 45; otherwise flagged (still added)
        player.setEligibilityStatus(player.determineEligibility(18, 45));
        players.add(player);
        return "Player added successfully (ID " + player.getPlayerId()
                + ", " + player.getEligibilityStatus() + ").";
    }

    // CREATE (batch from file) ----------
    /**
     * Loads players from a pipe-delimited text file, one per line.
     * Skips malformed or invalid lines instead of crashing, and returns
     * a summary of how many loaded vs. skipped.
     * Line format: id|first|last|yyyy-MM-dd|sport|jersey|teamId|status
     */
    public String loadFromFile(String filePath) {
        int loaded = 0;
        int skipped = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // ignore blank lines and comments
                }
                Player parsed = parseLine(line);
                if (parsed == null) {
                    skipped++;
                    continue;
                }
                String result = addPlayer(parsed);
                if (result.startsWith("Player added")) {
                    loaded++;
                } else {
                    skipped++;
                }
            }
        } catch (IOException e) {
            return "Error: could not read file at \"" + filePath + "\". No data was loaded.";
        }
        return "Batch load complete. " + loaded + " loaded, " + skipped + " skipped.";
    }

    /**
     * Parses one text line into a Player, returning null if it is malformed
     * (wrong field count, non-numeric numbers, bad date). Returning null
     * instead of throwing keeps the batch loader crash-proof.
     */
    private Player parseLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 8) {
            return null;
        }
        try {
            int id = Integer.parseInt(parts[0].trim());
            String first = parts[1].trim();
            String last = parts[2].trim();
            LocalDate dob = LocalDate.parse(parts[3].trim());
            String sport = parts[4].trim();
            int jersey = Integer.parseInt(parts[5].trim());
            int teamId = Integer.parseInt(parts[6].trim());
            String status = parts[7].trim();
            if (first.isEmpty() || last.isEmpty() || sport.isEmpty()) {
                return null;
            }
            return new Player(id, first, last, dob, sport, jersey, teamId, status);
        } catch (NumberFormatException | DateTimeParseException e) {
            return null; // bad number or date -> skip this line
        }
    }

    //  READ (display all)
    /**
     * Returns every player as a formatted multi-line String.
     * Returns text rather than printing, so the CLI controls output.
     */
    public String getAllPlayersDisplay() {
        if (players.isEmpty()) {
            return "There are no players in the system yet.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("--- All players (").append(players.size()).append(" total) ---\n");
        for (Player p : players) {
            sb.append(p.toDisplayString()).append("\n");
        }
        return sb.toString();
    }

    // helper: find by ID
    /** Finds a player by ID, returning the Player or null if not found. */
    public Player findById(int playerId) {
        for (Player p : players) {
            if (p.getPlayerId() == playerId) {
                return p;
            }
        }
        return null;
    }

    /** Reports whether a given player ID already exists. */
    public boolean idExists(int playerId) {
        return findById(playerId) != null;
    }

    // DELETE
    /**
     * Removes the player with the given ID.
     * Returns a message stating whether a player was actually removed.
     */
    public String removePlayerById(int playerId) {
        Player target = findById(playerId);
        if (target == null) {
            return "No player found with ID " + playerId + ". Nothing was removed.";
        }
        players.remove(target);
        return "Player ID " + playerId + " (" + target.getFirstName() + " "
                + target.getLastName() + ") was removed.";
    }

    /**
     * Updates one field of an existing player, chosen by field number.
     * After applying the change, re-runs full validation. If the new value
     * breaks a rule (duplicate jersey, bad sport, out-of-range age), the change
     * is ROLLED BACK to the old value and an error is returned. This makes the
     * update path as safe as the add path — bad data can never be saved.
     * Returns a message describing the outcome.
     */
    public String updatePlayerField(int playerId, int fieldChoice, String newValue) {
        Player p = findById(playerId);
        if (p == null) {
            return "No player found with ID " + playerId + ".";
        }

        // Remember the current values so we can roll back if the change is invalid
        String oldFirst = p.getFirstName();
        String oldLast = p.getLastName();
        LocalDate oldDob = p.getDateOfBirth();
        String oldSport = p.getSport();
        int oldJersey = p.getJerseyNumber();
        int oldTeam = p.getTeamId();
        String oldStatus = p.getEligibilityStatus();

        // Apply the requested change (bad number/date formats are caught here)
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
        } catch (DateTimeParseException e) {
            return "Update failed: date must be in the format yyyy-MM-dd.";
        }

        // Re-check business rules now that the change is applied
        String validationError = validator.getValidationError(p, players);
        if (validationError != null) {
            // Roll back every field to its previous value
            p.setFirstName(oldFirst);
            p.setLastName(oldLast);
            p.setDateOfBirth(oldDob);
            p.setSport(oldSport);
            p.setJerseyNumber(oldJersey);
            p.setTeamId(oldTeam);
            p.setEligibilityStatus(oldStatus);
            return "Update rejected: " + validationError + " No changes were made.";
        }

        // If age changed, refresh eligibility to stay consistent
        p.setEligibilityStatus(p.determineEligibility(18, 45));

        return "Player ID " + playerId + " updated successfully.";
    }

    // CUSTOM ACTION (math calculation)
    /**
     * Custom action: calculates the AVERAGE AGE of all players on a given team.
     * A real mathematical calculation (mean of ages) tied to the league data.
     * Returns the average as a double, or -1 if the team has no players,
     * so the caller can report that cleanly instead of dividing by zero.
     */
    public double calculateAverageAgeForTeam(int teamId) {
        int total = 0;
        int count = 0;
        for (Player p : players) {
            if (p.getTeamId() == teamId) {
                total += p.calculateAge();
                count++;
            }
        }
        if (count == 0) {
            return -1; // signal: no players on this team
        }
        return (double) total / count;
    }

    /** Returns how many players are on a given team. */
    public int countPlayersOnTeam(int teamId) {
        int count = 0;
        for (Player p : players) {
            if (p.getTeamId() == teamId) {
                count++;
            }
        }
        return count;
    }

    /** Returns how many players are in the system. */
    public int size() {
        return players.size();
    }
}