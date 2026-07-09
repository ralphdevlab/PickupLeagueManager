package test;

import com.pickupleague.Player;
import com.pickupleague.PlayerList;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Pickup League Manager logic layer.
 * Each required feature has an AFFIRMATIVE test (proves it works when it should)
 * and a NEGATIVE test (proves it fails/handles the bad case correctly).
 * These test the PlayerList logic directly using dummy Player objects,
 * with no CLI or database involved.
 */
public class PlayerListTest {

    // Helper: builds a valid dummy player so each test starts from good data.
    private Player makePlayer(int id, int jersey, int teamId, int birthYear) {
        return new Player(id, "Test", "Player",
                LocalDate.of(birthYear, 1, 1), "Soccer", jersey, teamId, "Pending");
    }

    // FEATURE: Add an object

    @Test
    public void addPlayer_validPlayer_succeeds() {
        PlayerList list = new PlayerList();
        String result = list.addPlayer(makePlayer(1, 10, 5, 2000));
        assertTrue(result.contains("successfully"));   // affirmative: it was added
        assertEquals(1, list.size());
    }

    @Test
    public void addPlayer_duplicateJerseyOnSameTeam_fails() {
        PlayerList list = new PlayerList();
        list.addPlayer(makePlayer(1, 10, 5, 2000));
        String result = list.addPlayer(makePlayer(2, 10, 5, 1999)); // same jersey, same team
        assertTrue(result.contains("Could not add"));  // negative: rejected
        assertEquals(1, list.size());                  // second player NOT added
    }

    // FEATURE: Remove an object

    @Test
    public void removePlayer_existingId_succeeds() {
        PlayerList list = new PlayerList();
        list.addPlayer(makePlayer(1, 10, 5, 2000));
        String result = list.removePlayerById(1);
        assertTrue(result.contains("was removed"));    // affirmative
        assertEquals(0, list.size());                  // list is now empty
    }

    @Test
    public void removePlayer_nonexistentId_isHandled() {
        PlayerList list = new PlayerList();
        list.addPlayer(makePlayer(1, 10, 5, 2000));
        String result = list.removePlayerById(999);    // no such player
        assertTrue(result.contains("No player found")); // negative: handled, not crashed
        assertEquals(1, list.size());                  // nothing was removed
    }

    //  FEATURE: Update an object

    @Test
    public void updatePlayer_validChange_succeeds() {
        PlayerList list = new PlayerList();
        list.addPlayer(makePlayer(1, 10, 5, 2000));
        String result = list.updatePlayerField(1, 5, "42"); // field 5 = jersey number
        assertTrue(result.contains("updated successfully"));   // affirmative
        assertEquals(42, list.findById(1).getJerseyNumber());  // value actually changed
    }

    @Test
    public void updatePlayer_invalidNumber_isHandled() {
        PlayerList list = new PlayerList();
        list.addPlayer(makePlayer(1, 10, 5, 2000));
        String result = list.updatePlayerField(1, 5, "abc"); // letters for a number field
        assertTrue(result.contains("whole number"));    // negative: rejected, not crashed
        assertEquals(10, list.findById(1).getJerseyNumber()); // value unchanged
    }

    //  FEATURE: Custom action (average age)

    @Test
    public void averageAge_teamWithPlayers_calculatesCorrectly() {
        PlayerList list = new PlayerList();
        list.addPlayer(makePlayer(1, 10, 5, 2000)); // both on team 5
        list.addPlayer(makePlayer(2, 11, 5, 1990));
        double avg = list.calculateAverageAgeForTeam(5);
        int p1 = list.findById(1).calculateAge();
        int p2 = list.findById(2).calculateAge();
        assertEquals((p1 + p2) / 2.0, avg, 0.01);      // affirmative: math is right
    }

    @Test
    public void averageAge_emptyTeam_returnsNegativeOne() {
        PlayerList list = new PlayerList();
        double avg = list.calculateAverageAgeForTeam(99); // no players on team 99
        assertEquals(-1, avg, 0.01);                   // negative: signals "no players"
    }

    // FEATURE: A file can be opened

    @Test
    public void loadFromFile_missingFile_isHandled() {
        PlayerList list = new PlayerList();
        String result = list.loadFromFile("wrong_file.txt");
        assertTrue(result.contains("could not read file")); // negative: handled gracefully
        assertEquals(0, list.size());                  // nothing loaded
    }
}
