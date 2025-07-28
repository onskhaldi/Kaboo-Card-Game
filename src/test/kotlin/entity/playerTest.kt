package entity
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.*

/**
 *  tests for the [Player] class.
 *
 * These tests verify:
 * - That the player's name is correctly assigned
 * - That the player's hand is initially empty (contains only nulls)
 * - That the drawn card is initially null
 * - That the string representation of the player is correct
 * - That cards can be correctly placed into the player's hand
 */

class PlayerTest {

    private val player = Player(name = "Alex")

    /**
     * Verifies that the player's name is set correctly.
     */
    @Test
    fun testName () {
        assertEquals( expected = "Alex", actual = player.name,
            "should return the exact player´s name - Alex"
        )
    }

    /**
     * Verifies that all positions in the player's hand are null when the game starts.
     */
    @Test
    fun initialHandIsEmpty() {
        for (row in player.hand) {
            for (card in row) {
                assertNull(card, "All positions in the player's hand should be null at game start.")
            }
        }
    }

    /**
     * Verifies that the drawn card is null at the beginning.
     */
    @Test
    fun initialDrawnCardIsNull() {
        assertNull(player.drawnCard, "drawnCard should be null at the beginning.")
    }
    /**
     * Verifies the string representation of the player.
     */
    @Test
    fun testToStringPlayer() {
        val expected = "Alex: Hand0 Cards"
        val actual = player.toString()
        assertEquals(expected, actual, "The toString output is incorrect.")
    }
    /**
     * Verifies that a card can be correctly placed into the player's hand.
     */
    @Test
    fun addCardToHand() {
    val card = Card(CardSuit.HEARTS, CardValue.SEVEN, isPowercard = false)
        player.hand[0][0] = card
        assertEquals(card, player.hand[0][0], "Card should be correctly placed into hand.")
    }

}


