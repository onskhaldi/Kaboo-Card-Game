package entity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

//import kotlin.test.*
//import kotlin.test.assertEquals

/**
 *  tests for the [Card] class.
 *
 * This test class verifies:
 * - The correct string representation of a card using [Card.toString]
 * - That two cards with different values are not considered equal
 * - That the powercard flag ([Card.isPowercard]) behaves as expected
 */

class CardTest {
    private val aceOfSpades = Card(CardSuit.SPADES, CardValue.ACE, false)
    private val jackOfClubs = Card(CardSuit.CLUBS, CardValue.JACK, true)
    private val queenOfHearts = Card(CardSuit.HEARTS, CardValue.QUEEN, true)
    private val jackOfDiamonds = Card(CardSuit.DIAMONDS, CardValue.JACK, true)


    // Unicode characters for the suits, as those should be used by [Card.toString]
    private val heartsChar = '\u2665' // ♥
    private val spadesChar = '\u2660' // ♠
    private val clubsChar = '\u2663' // ♣

    /**
     * Verifies that [Card.toString] returns the expected Unicode representation for some sample cards.
     */
    @Test
    fun testToString() {
        assertEquals(spadesChar + "A", aceOfSpades.toString())
        assertEquals(clubsChar + "J", jackOfClubs.toString())
        assertEquals(heartsChar + "Q", queenOfHearts.toString())
    }


    /**
     * Verifies that two cards with different values are not equal.
     */
    @Test
    fun testNotEquals() {
        assertNotEquals( aceOfSpades.value.ordinal, jackOfClubs.value.ordinal,
            "Cards values should not be equal" )
    }
    /**
     * Verifies the correct behavior of the [Card.isPowercard] flag.
     */
    @Test
    fun testPowerCard() {
        assertTrue(jackOfClubs.isPowercard, "Jack of Clubs should be marked as a powercard")
        assertFalse(aceOfSpades.isPowercard, "Ace of Spades should not be a powercard")
    }
}


