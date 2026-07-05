package com.pickupleague;

import java.util.List;

/**
 * Holds all the business validation rules for a Player.
 * Every method RETURNS a value (a boolean or an error String) instead of
 * being void, which keeps the rules easy to test and reuse.
 * This is the class that stops "bad" data from ever entering the system.
 */
public class PlayerValidator {

    // The league's allowed age range, set once when the validator is created
    private int minAge;
    private int maxAge;

    // Constructor: the CLI creates this with the league's age limits
    public PlayerValidator(int minAge, int maxAge) {
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    /**
     * Checks whether a player's age falls within the allowed range.
     * Returns true if the age is valid, false otherwise.
     */
    public boolean isAgeValid(Player player) {
        int age = player.calculateAge();
        return age >= minAge && age <= maxAge;
    }

    /**
     * Checks that no OTHER player on the same team already wears this jersey number.
     * Skips the player being checked (by ID) so editing a player doesn't clash
     * with itself. Returns true if the number is free to use.
     */
    public boolean isJerseyNumberUnique(Player player, List<Player> existingPlayers) {
        for (Player other : existingPlayers) {
            if (other.getPlayerId() != player.getPlayerId()
                    && other.getTeamId() == player.getTeamId()
                    && other.getJerseyNumber() == player.getJerseyNumber()) {
                return false; // another player on this team already has this number
            }
        }
        return true;
    }

    /**
     * Runs every rule and returns the FIRST error found as a message String,
     * or null if the player passes all checks. Both the manual-add path and the
     * file-load path call this, so the rules are identical everywhere.
     */
    public String getValidationError(Player player, List<Player> existingPlayers) {
        if (!isNameValid(player.getFirstName())) {
            return "first name must be letters only (no numbers or symbols) and not empty.";
        }
        if (!isNameValid(player.getLastName())) {
            return "last name must be letters only (no numbers or symbols) and not empty.";
        }
        if (player.getSport() == null
                || (!player.getSport().equalsIgnoreCase("Soccer")
                && !player.getSport().equalsIgnoreCase("Basketball"))) {
            return "sport must be Soccer or Basketball.";
        }
        if (player.getJerseyNumber() < 0 || player.getJerseyNumber() > 99) {
            return "jersey number must be between 0 and 99.";
        }
        if (player.getTeamId() < 1 || player.getTeamId() > 999) {
            return "team ID must be between 1 and 999.";
        }
        if (player.getPlayerId() < 1 || player.getPlayerId() > 9999) {
            return "player ID must be between 1 and 9999.";
        }
        if (!isAgeValid(player)) {
            return "player's age must be between " + minAge + " and " + maxAge + ".";
        }
        if (!isJerseyNumberUnique(player, existingPlayers)) {
            return "jersey number " + player.getJerseyNumber()
                    + " is already taken on team " + player.getTeamId() + ".";
        }
        return null;
    }

    /**
     * Checks whether a name is valid: not empty, not too long, and made only of
     * letters and spaces (so "Mary Jane" works, but "12345", "@#$", and blanks do not).
     * Returns true if the name is acceptable.
     */
    public boolean isNameValid(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > 40) {
            return false;
        }
        // letters and spaces only
        return trimmed.matches("[a-zA-Z][a-zA-Z ]*");
    }
}
