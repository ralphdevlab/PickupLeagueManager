package com.pickupleague;

import java.util.List;

/**
 * Enforces the league's business rules for a {@link Player}.
 * <p>
 * This class is the single place where "what counts as valid data" is defined.
 * Both the graphical interface and the database layer call into it, so a player
 * typed in by hand and a player loaded from the database are held to exactly the
 * same standards.
 * <p>
 * Every method returns a value rather than throwing or printing, which keeps the
 * rules easy to test and lets the caller decide how to report a problem.
 *
 * @author Ralph Alexandre
 * @version 4.0
 */
public class PlayerValidator {

    /** The youngest age a player may be and still be stored in the system. */
    private int minAge;

    /** The oldest age a player may be and still be stored in the system. */
    private int maxAge;

    /**
     * Creates a validator that accepts players within the given age range.
     * <p>
     * Note that this range is deliberately wider than the range used for
     * eligibility. A fifteen year old can be recorded in the system and flagged
     * as underage, but an age of three would be rejected as bad data.
     *
     * @param minAge the youngest age that may be stored, inclusive
     * @param maxAge the oldest age that may be stored, inclusive
     */
    public PlayerValidator(int minAge, int maxAge) {
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    /**
     * Checks whether a player's age falls within the range this validator
     * accepts.
     *
     * @param p the player whose age should be checked
     * @return true if the player's age is within the allowed range
     */
    public boolean isAgeValid(Player p) {
        int age = p.calculateAge();
        return age >= minAge && age <= maxAge;
    }

    /**
     * Checks whether a name is acceptable. A valid name starts with a letter,
     * contains only letters and spaces, is not empty, and is no longer than
     * forty characters.
     *
     * @param name the name to check; may be null
     * @return true if the name is acceptable, false if it is null, empty, too
     *         long, or contains digits or symbols
     */
    public boolean isNameValid(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > 40) {
            return false;
        }
        return trimmed.matches("[a-zA-Z][a-zA-Z ]*");
    }

    /**
     * Checks that no other player on the same team already wears this player's
     * jersey number. The player being checked is skipped by ID, so editing a
     * player never conflicts with their own existing number.
     *
     * @param player   the player whose jersey number should be checked
     * @param existing every player currently in the system
     * @return true if the number is free to use on that team
     */
    public boolean isJerseyNumberUnique(Player player, List<Player> existing) {
        for (Player other : existing) {
            if (other.getPlayerId() != player.getPlayerId()
                    && other.getTeamId() == player.getTeamId()
                    && other.getJerseyNumber() == player.getJerseyNumber()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Runs every validation rule against a player and reports the first problem
     * found. This is the method callers should use before storing a player.
     * <p>
     * The rules checked, in order, are: first and last name are letters only,
     * the sport is one that is supported, the jersey number is in range, the
     * team and player IDs are in range, the age is within the storable range,
     * and the jersey number is not already taken on that team.
     *
     * @param p        the player to validate
     * @param existing every player currently in the system, used for the
     *                 jersey number uniqueness check
     * @return null if the player passes every rule, otherwise a message
     *         describing the first rule that failed
     */
    public String getValidationError(Player p, List<Player> existing) {
        if (!isNameValid(p.getFirstName())) {
            return "first name must be letters only (no numbers or symbols) and not empty.";
        }
        if (!isNameValid(p.getLastName())) {
            return "last name must be letters only (no numbers or symbols) and not empty.";
        }
        if (p.getSport() == null
                || (!p.getSport().equalsIgnoreCase("Soccer")
                && !p.getSport().equalsIgnoreCase("Basketball"))) {
            return "sport must be Soccer or Basketball.";
        }
        if (p.getJerseyNumber() < 0 || p.getJerseyNumber() > 99) {
            return "jersey number must be between 0 and 99.";
        }
        if (p.getTeamId() < 1 || p.getTeamId() > 999) {
            return "team ID must be between 1 and 999.";
        }
        if (p.getPlayerId() < 1 || p.getPlayerId() > 9999) {
            return "player ID must be between 1 and 9999.";
        }
        if (!isAgeValid(p)) {
            return "player's age must be between " + minAge + " and " + maxAge + ".";
        }
        if (!isJerseyNumberUnique(p, existing)) {
            return "jersey number " + p.getJerseyNumber()
                    + " is already taken on team " + p.getTeamId() + ".";
        }
        return null;
    }
}