package entity

import kotlin.test.*

class CardTest {

    @Test
    fun testCardInitializationAndToString() {
        val card = Card(CardSuit.HEARTS, CardValue.SEVEN, true)

        assertEquals(CardSuit.HEARTS, card.suit)
        assertEquals(CardValue.SEVEN, card.value)
        assertTrue(card.isPowercard)
        assertEquals("♥7", card.toString())
    }

    @Test
    fun testCardEquality() {
        val card1 = Card(CardSuit.SPADES, CardValue.KING, false)
        val card2 = Card(CardSuit.SPADES, CardValue.KING, false)

        assertEquals(card1, card2)
        assertEquals(card1.hashCode(), card2.hashCode())
    }

    @Test
    fun testCardCopy() {
        val original = Card(CardSuit.CLUBS, CardValue.TEN, true)
        val copy = original.copy()

        assertEquals(original, copy)
        assertNotSame(original, copy)
    }

    @Test
    fun testMutablePowerCardFlag() {
        val card = Card(CardSuit.DIAMONDS, CardValue.ACE, false)
        card.isPowercard = true
        assertTrue(card.isPowercard)
    }
}

