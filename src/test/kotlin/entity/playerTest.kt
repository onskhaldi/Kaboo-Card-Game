package entity

import kotlin.test.*

class PlayerTest {

    @Test
    fun testPlayerInitialization() {
        val player = Player("Alice")

        assertEquals("Alice", player.name)
        assertNull(player.drawnCard)
        assertEquals(2, player.hand.size)
        assertEquals(2, player.hand[0].size)
        assertTrue(player.hand.all { row -> row.all { it == null } })
    }

    @Test
    fun testSetAndGetCardsInHand() {
        val player = Player("Bob")
        val card = Card(CardSuit.SPADES, CardValue.KING, isPowercard = false)

        player.hand[1][0] = card
        assertEquals(card, player.hand[1][0])
        assertNull(player.hand[0][1])
    }

    @Test
    fun testDrawnCard() {
        val player = Player("Charlie")
        assertNull(player.drawnCard)

        val drawn = Card(CardSuit.HEARTS, CardValue.ACE, isPowercard = true)
        player.drawnCard = drawn
        assertEquals(drawn, player.drawnCard)
    }

    @Test
    fun testToStringRepresentation() {
        val player = Player("Dora")
        assertEquals("Dora: Hand0 Cards", player.toString())

        val card1 = Card(CardSuit.CLUBS, CardValue.FOUR, false)
        val card2 = Card(CardSuit.DIAMONDS, CardValue.SEVEN, true)
        player.hand[0][0] = card1
        player.hand[1][1] = card2

        assertEquals("Dora: Hand2 Cards", player.toString())
    }
}
