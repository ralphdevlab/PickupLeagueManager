package com.pickupleague;

import java.time.LocalDate;
import java.time.Period;

/**
 * The core object of the Pickup League Manager.
 * Represents one player in a soccer or basketball pickup league.
 * Holds 8 pieces of data of varying types (int, String, LocalDate),
 * satisfying the "at least 6 attributes of various data types" requirement.
 */
public class Player {

    // Attributes
    private int playerId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String sport;
    private int jerseyNumber;
    private int teamId;
    private String eligibilityStatus;

    // Constructor: builds a Player with all fields set at once
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

    // Getters: return each field's value
    public int getPlayerId() { return playerId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getSport() { return sport; }
    public int getJerseyNumber() { return jerseyNumber; }
    public int getTeamId() { return teamId; }
    public String getEligibilityStatus() { return eligibilityStatus; }

    // Setters: update each field
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setSport(String sport) { this.sport = sport; }
    public void setJerseyNumber(int jerseyNumber) { this.jerseyNumber = jerseyNumber; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public void setEligibilityStatus(String eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }

    /**
     * Calculates the player's current age in whole years from date of birth.
     * Returns a value (int) instead of being void, following the design rules.
     */
    public int calculateAge() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Determines this player's eligibility from their age and the league's
     * age range, and returns the resulting status string.
     * Returns a value rather than being void, following the design rules.
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

    /**
     * Returns one formatted line describing this player, used when displaying all data.
     * Returns text rather than printing, which keeps the object testable.
     */
    public String toDisplayString() {
        return String.format(
                "ID %-3d | %-18s | %-10s | #%-3d | TeamID %-3d | Age %-3d | %s",
                playerId, firstName + " " + lastName, sport, jerseyNumber, teamId,
                calculateAge(), eligibilityStatus);
    }

    /**
     * Converts this player back into a pipe-delimited text-file line,
     * matching the format the data was loaded from.
     */
    public String toFileLine() {
        return playerId + "|" + firstName + "|" + lastName + "|" + dateOfBirth + "|"
                + sport + "|" + jerseyNumber + "|" + teamId + "|" + eligibilityStatus;
    }
}