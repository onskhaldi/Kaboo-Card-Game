package entity

import kotlin.test.*

class KabooGameTest {

    @Test
    fun testKabooGameInitialization() {
        val player1 = Player("Alice")
        val player2 = Player("Bob")

        val game = KabooGame(
            player1 = player1,
            player2 = player2,
            currentPlayer = 0
        )

        assertEquals(player1, game.player1)
        assertEquals(player2, game.player2)
        assertEquals(0, game.currentPlayer)
        assertNull(game.state)
        assertTrue(game.selected.isEmpty())
        assertTrue(game.drawPile.isEmpty())
        assertTrue(game.playStack.isEmpty())
        assertFalse(game.lastRound)
        assertTrue(game.log.isEmpty())
    }

    @Test
    fun testMutableGameProperties() {
        val player1 = Player("Alice")
        val player2 = Player("Bob")
        val game = KabooGame(player1, player2, 1)

        // state ändern
        game.state = GamePhase.DRAW
        assertEquals(GamePhase.DRAW, game.state)

        // Karten zur Selektion hinzufügen
        val card = Card(CardSuit.HEARTS, CardValue.SEVEN, true)
        game.selected.add(card)
        assertEquals(1, game.selected.size)
        assertEquals(card, game.selected[0])

        // Spielstapel testen
        game.drawPile.add(Card(CardSuit.CLUBS, CardValue.TEN, false))
        assertEquals(1, game.drawPile.size)

        // Logeinträge
        game.log.add("Spiel gestartet.")
        assertEquals("Spiel gestartet.", game.log.first())
    }

    @Test
    fun testGameEquality() {
        val player1 = Player("Alice")
        val player2 = Player("Bob")
        val game1 = KabooGame(player1, player2, 0)
        val game2 = game1.copy()

        assertEquals(game1.player1, game2.player1)
        assertEquals(game1.player2, game2.player2)
        assertEquals(game1.currentPlayer, game2.currentPlayer)
        assertNotSame(game1, game2)
    }
}

