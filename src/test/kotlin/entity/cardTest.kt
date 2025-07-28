package entity

import kotlin.test.*
/**
 * Tests für die [Card]-Klasse.
 */
class CardTest {
    /**
     * Testet die Initialisierung einer Karte sowie deren [toString]-Darstellung.
     *
     * Überprüft:
     * - Farbe (suit), Wert (value) und Powercard-Flag
     * - korrekte String-Darstellung, z. B. "♥7"
     */
    @Test
    fun testCardInitializationAndToString() {
        val card = Card(CardSuit.HEARTS, CardValue.SEVEN, true)

        assertEquals(CardSuit.HEARTS, card.suit)
        assertEquals(CardValue.SEVEN, card.value)
        assertTrue(card.isPowercard)
        assertEquals("♥7", card.toString())
    }

    /**
     * Testet Gleichheit von Karten.
     *
     * Überprüft:
     * - equals() erkennt identische Karten korrekt
     */
    @Test
    fun testCardEquality() {
        val card1 = Card(CardSuit.SPADES, CardValue.KING, false)
        val card2 = Card(CardSuit.SPADES, CardValue.KING, false)

        assertEquals(card1, card2)
        assertEquals(card1.hashCode(), card2.hashCode())
    }

    /**
     * Testet die `copy()`-Funktion der Karte.
     *
     * Überprüft:
     * - Inhaltliche Gleichheit der Kopie
     * - Unterschiedliche Referenzen (nicht dasselbe Objekt)
     */
    @Test
    fun testCardCopy() {
        val original = Card(CardSuit.CLUBS, CardValue.TEN, true)
        val copy = original.copy()

        assertEquals(original, copy)
        assertNotSame(original, copy)
    }
    /**
     * Testet das Verändern des `isPowercard nach der Initialisierung.
     */
    @Test
    fun testPowerCard() {
        val card = Card(CardSuit.DIAMONDS, CardValue.ACE, false)
        card.isPowercard = true
        assertTrue(card.isPowercard)
    }
}

