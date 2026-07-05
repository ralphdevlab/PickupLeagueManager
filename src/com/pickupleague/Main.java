package com.pickupleague;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Application entry point for the Pickup League Manager .
 * Displays a menu, reads the user's choice, and calls the matching operation
 * on the PlayerList. All input is read as text and carefully validated so the
 * user can never crash the program, and the app exits only when the user asks.
 *
 * main() is static because Java requires it, but it does almost nothing:
 * it creates a Main object and calls the instance method run(). All real
 * logic lives in non-static methods, satisfying the "no static methods" rule.
 */
public class Main {

    private final PlayerList playerList;
    private final Scanner scanner;

    // Constructor: sets up the data store and the input reader
    public Main() {
        this.playerList = new PlayerList();
        this.scanner = new Scanner(System.in);
    }

    // The only static method — hands off immediately to instance code
    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    /**
     * The main program loop. Keeps showing the menu until the user chooses exit.
     * Returns a goodbye message when the loop ends.
     */
    public String run() {
        boolean running = true;
        System.out.println("=== Pickup League Manager ===");
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1: doLoadFromFile(); break;
                case 2: doAddPlayer(); break;
                case 3: System.out.println(playerList.getAllPlayersDisplay()); break;
                case 4: doUpdatePlayer(); break;
                case 5: doRemovePlayer(); break;
                case 6: doCustomAction(); break;
                case 7:
                    running = false;
                    break;
                default:
                    System.out.println("Please choose a number from 1 to 7.");
            }
        }
        String goodbye = "Goodbye!";
        System.out.println(goodbye);
        return goodbye;
    }

    // Prints the menu of options
    private void printMenu() {
        System.out.println("\n----------------------------------------");
        System.out.println("1. Load players from a text file (batch)");
        System.out.println("2. Add a player manually");
        System.out.println("3. Display all players");
        System.out.println("4. Update a player");
        System.out.println("5. Remove a player");
        System.out.println("6. Custom action: average age of a team");
        System.out.println("7. Exit");
        System.out.println("----------------------------------------");
    }

    // menu action handlers

    private void doLoadFromFile() {
        System.out.print("Enter the path to the data file: ");
        String path = scanner.nextLine().trim();
        System.out.println(playerList.loadFromFile(path));
    }

    private void doAddPlayer() {
        int id = readIntRange("Player ID (1-9999): ", 1, 9999);
        if (playerList.idExists(id)) {
            System.out.println("A player with ID " + id + " already exists. Cancelled.");
            return;
        }
        System.out.print("First name: ");
        String first = scanner.nextLine().trim();
        System.out.print("Last name: ");
        String last = scanner.nextLine().trim();
        LocalDate dob = readDate("Date of birth (yyyy-MM-dd): ");
        String sport = readSport("Sport (Soccer/Basketball): ");
        int jersey = readIntRange("Jersey number (0-99): ", 0, 99);
        int teamId = readIntRange("Team ID (1-999): ", 1, 999);

        Player p = new Player(id, first, last, dob, sport, jersey, teamId, "Pending");
        System.out.println(playerList.addPlayer(p));
    }

    private void doUpdatePlayer() {
        int id = readInt("Enter the ID of the player to update: ");
        if (!playerList.idExists(id)) {
            System.out.println("No player found with ID " + id + ".");
            return;
        }
        System.out.println("Which field? 1=First 2=Last 3=DOB 4=Sport 5=Jersey 6=TeamID 7=Status");
        int field = readInt("Field number: ");
        System.out.print("New value: ");
        String newValue = scanner.nextLine().trim();
        System.out.println(playerList.updatePlayerField(id, field, newValue));
    }

    private void doRemovePlayer() {
        int id = readInt("Enter the ID of the player to remove: ");
        System.out.println(playerList.removePlayerById(id));
    }

    private void doCustomAction() {
        int teamId = readInt("Enter the Team ID to average: ");
        double avg = playerList.calculateAverageAgeForTeam(teamId);
        if (avg < 0) {
            System.out.println("No players found on team " + teamId + ".");
        } else {
            int count = playerList.countPlayersOnTeam(teamId);
            System.out.printf("Team %d has %d player(s), average age %.1f years.%n",
                    teamId, count, avg);
        }
    }

    // ---------- safe input helpers ----------

    /**
     * Reads a whole number that must fall between min and max (inclusive),
     * re-prompting IMMEDIATELY on anything invalid — non-numbers, negatives,
     * or values outside the allowed digit range. Nothing bad gets through.
     */
    private int readIntRange(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(raw);
                if (value < min || value > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                    continue;   // bad range -> ask again right now
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("That wasn't a whole number. Try again.");
            }
        }
    }

    /**
     * Reads a sport, accepting only Soccer or Basketball (case-insensitive).
     * Re-prompts immediately on anything else, and returns the clean,
     * properly-capitalized version.
     */
    private String readSport(String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            if (raw.equalsIgnoreCase("Soccer")) {
                return "Soccer";
            }
            if (raw.equalsIgnoreCase("Basketball")) {
                return "Basketball";
            }
            System.out.println("Sport must be either Soccer or Basketball. Try again.");
        }

    }
    /**
     * Reads a whole number, re-prompting until the user actually types one.
     * Used by menu options that don't need a strict range.
     */
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                System.out.println("That wasn't a whole number. Try again.");
            }
        }
    }

    /**
     * Reads a date in yyyy-MM-dd form, re-prompting until it's valid.
     */
    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use the format yyyy-MM-dd (e.g. 2001-08-15).");
            }
        }
    }
}