package com.pickupleague;

import java.time.LocalDate;
import java.time.Period;

/**
 * Represents a single player in a soccer or basketball pickup league.
 * <p>
 * This is the core data object of the Pickup League Manager. Each Player holds
 * eight pieces of information of varying data types, and knows how to calculate
 * its own age and determine its own eligibility to play.
 * <p>
 * Player is a plain data class: it does not read or write to the database
 * itself. {@link PlayerList} is responsible for storing and retrieving Player
 * objects, and {@link PlayerValidator} is responsible for checking that a
 * Player's values follow the league's rules.
 *
 * @author Ralph Alexandre
 * @version 4.0
 */
public class Player {

    /** Unique identifier for this player, between 1 and 9999. */
    private int playerId;

    /** The player's first name; letters and spaces only. */
    private String firstName;

    /** The player's last name; letters and spaces only. */
    private String lastName;

    /** The player's date of birth, used to calculate age and eligibility. */
    private LocalDate dateOfBirth;

    /** The sport this player competes in: either Soccer or Basketball. */
    private String sport;

    /** The player's jersey number, between 0 and 99, unique within a team. */
    private int jerseyNumber;

    /** The identifier of the team this player belongs to, between 1 and 999. */
    private int teamId;

    /** Whether the player is eligible to play, or the reason they are not. */
    private String eligibilityStatus;

    /**
     * Creates a new Player with every field supplied at once.
     * <p>
     * This constructor performs no validation. Callers should pass the new
     * Player to {@link PlayerList#addPlayer(Player)}, which validates it before
     * storing it.
     *
     * @param playerId          the unique identifier for this player
     * @param firstName         the player's first name
     * @param lastName          the player's last name
     * @param dateOfBirth       the player's date of birth
     * @param sport             the sport played, either Soccer or Basketball
     * @param jerseyNumber      the number worn by this player
     * @param teamId            the identifier of the player's team
     * @param eligibilityStatus the player's current eligibility status
     */
    public Player(int playerId, String firstName, String lastName, LocalDate dateOfBirth,
                  String sport, int jerseyNumber, int teamId, String eligibilityStatus) {
        this.playerId = playerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.sport = sport;
        this.jerseyNumber = jerseyNumber;
        this.teamId = teamId;
        this.eligibilityStatus = eligibilityStatus;
    }

    /**
     * Returns this player's unique identifier.
     *
     * @return the player's ID number
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Returns this player's first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns this player's last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns this player's date of birth.
     *
     * @return the date of birth
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Returns the sport this player competes in.
     *
     * @return either "Soccer" or "Basketball"
     */
    public String getSport() {
        return sport;
    }

    /**
     * Returns this player's jersey number.
     *
     * @return the jersey number, between 0 and 99
     */
    public int getJerseyNumber() {
        return jerseyNumber;
    }

    /**
     * Returns the identifier of the team this player belongs to.
     *
     * @return the team ID
     */
    public int getTeamId() {
        return teamId;
    }

    /**
     * Returns this player's current eligibility status.
     *
     * @return "Eligible", "Ineligible - Underage", or "Ineligible - Overage"
     */
    public String getEligibilityStatus() {
        return eligibilityStatus;
    }

    /**
     * Sets this player's unique identifier.
     *
     * @param playerId the new ID number
     */
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    /**
     * Sets this player's first name.
     *
     * @param firstName the new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets this player's last name.
     *
     * @param lastName the new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets this player's date of birth. Changing this affects the values
     * returned by {@link #calculateAge()} and {@link #determineEligibility(int, int)}.
     *
     * @param dateOfBirth the new date of birth
     */
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Sets the sport this player competes in.
     *
     * @param sport the new sport, either "Soccer" or "Basketball"
     */
    public void setSport(String sport) {
        this.sport = sport;
    }

    /**
     * Sets this player's jersey number.
     *
     * @param jerseyNumber the new jersey number
     */
    public void setJerseyNumber(int jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    /**
     * Sets the team this player belongs to.
     *
     * @param teamId the new team identifier
     */
    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    /**
     * Sets this player's eligibility status.
     *
     * @param eligibilityStatus the new status text
     */
    public void setEligibilityStatus(String eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }

    /**
     * Calculates this player's current age in whole years, based on their date
     * of birth and today's date. Because the age is derived rather than stored,
     * it is always current and never becomes stale.
     *
     * @return the player's age in completed years
     */
    public int calculateAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Determines whether this player is old enough, and not too old, to play in
     * the league, and returns the matching status text. A player outside the
     * range is still a valid record; they are simply flagged as ineligible.
     *
     * @param minAge the youngest age allowed to play, inclusive
     * @param maxAge the oldest age allowed to play, inclusive
     * @return "Eligible" if the player's age falls within the range,
     *         "Ineligible - Underage" if they are below it, or
     *         "Ineligible - Overage" if they are above it
     */
    public String determineEligibility(int minAge, int maxAge) {
        int age = calculateAge();
        if (age < minAge) {
            return "Ineligible - Underage";
        }
        if (age > maxAge) {
            return "Ineligible - Overage";
        }
        return "Eligible";
    }
}