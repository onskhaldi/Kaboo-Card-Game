package entity

import kotlin.test.*

class PlayerTest {
    /**
     * Testet die Initialisierung eines neuen Spielers.
     * Überprüft:
     * - Name wird korrekt gesetzt
     * - Gezogene Karte ist anfangs null
     * - Hand besteht aus einem 2x2-Array mit nur null-Werten
     */
    @Test
    fun testPlayerInitialization() {
        val player = Player("Alice")

        assertEquals("Alice", player.name)
        assertNull(player.drawnCard)
        assertEquals(2, player.hand.size)
        assertEquals(2, player.hand[0].size)
        assertTrue(player.hand.all { row -> row.all { it == null } })
    }
    /**
     * Testet das Setzen und Abrufen einer Karte in der Hand.
     * Setzt eine Karte an Position [1][0] und prüft:
     * - Ob die Karte korrekt gespeichert wurde
     * - Ob andere Felder weiterhin null sind
     */
    @Test
    fun testSetAndGetCardsInHand() {
        val player = Player("Bob")
        val card = Card(CardSuit.SPADES, CardValue.KING, isPowercard = false)

        player.hand[1][0] = card
        assertEquals(card, player.hand[1][0])
        assertNull(player.hand[0][1])
    }
    /**
     * Testet das Setzen und Abrufen der gezogenen Karte.
     * Überprüft:
     * - Anfangswert ist null
     * - Nach Setzen ist die Karte korrekt gespeichert
     */
    @Test
    fun testDrawnCard() {
        val player = Player("Charlie")
        assertNull(player.drawnCard)

        val drawn = Card(CardSuit.HEARTS, CardValue.ACE, isPowercard = true)
        player.drawnCard = drawn
        assertEquals(drawn, player.drawnCard)
    }
    /**
     * Testet die Zeichenketten-Darstellung ([toString]) des Spielers.
     * Überprüft:
     * - Leere Hand zeigt "Hand0 Cards"
     * - Nach Setzen von zwei Karten zeigt Darstellung "Hand2 Cards"
     */
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
