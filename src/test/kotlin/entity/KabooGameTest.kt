package entity

import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.Assertions.*

/**
 *  tests for the [KabooGame] class.
 *
 * These tests verify:
 * - Initial player setup
 * - Empty draw pile and play stack at the start
 * - Current player initialization
 * - Initial state and selected cards list
 * - lastRound and game log default values
 */


class GameTest {
    private val player1 = Player(name = "Alex")
    private val player2 = Player(name = "Sam")
    private val game = KabooGame(player1, player2, currentPlayer = 1)

    /**
     * Verifies that the players are initialized with correct names.
     */
    @Test
    fun playerNameTest() {
        assertEquals("Alex", game.player1.name)
        assertEquals("Sam", game.player2.name)
    }

    /**
     * Verifies that the draw pile and play stack are empty at game start.
     */
    @Test
    fun drawPileAndExchangeAreaEmpty() {
        assertTrue(game.drawPile.isEmpty(), "Draw pile should be empty when the game starts")
        assertTrue(game.playStack.isEmpty(), "Exchange area should be empty when the game starts")
    }
    /**
     * Verifies that the current player is set correctly at the beginning.
     */
    @Test
    fun currentPlayerTest() {
        assertEquals(1, game.currentPlayer, "Current player should be 1 at game start")
    }

    /**
     * Verifies that the game state is null at the start.
     */
    @Test
    fun initialStateIsNull() {
        assertNull(game.state, "Game state should be null at game start")
    }
    /**
     * Verifies that the selected cards list is empty at the start.
     */

    @Test
    fun selectedListIsEmptyInitially() {
        assertTrue(game.selected.isEmpty(), "Selected list should be empty at game start")
    }
    /**
     * Verifies that lastRound is false at the beginning of the game.
     */
    @Test
    fun lastRoundIsFalseInitially() {
        assertFalse(game.lastRound, "lastRound should be false at game start")
    }
    /**
     * Verifies that the game log is empty when the game starts.
     */
    @Test
    fun logIsEmptyInitially() {
        assertTrue(game.log.isEmpty(), "Game log should be empty at game start")
    }
}



