package entity
import java.util.Stack

/**
 * Represents the full state of an active Kaboo game, including both players,
 * the current turn, the draw and play piles, selected cards, and game status flags.
 *
 * This class is used as the central data model that both the game logic and
 * GUI interact with to track and update the progress of the game.
 *
 * @property player1 The first player in the game.
 * @property player2 The second player in the game.
 * @property currentPlayer Index of the current player (0 for player1, 1 for player2).
 */
data class KabooGame (
    var player1: Player,
    var player2: Player,
    var currentPlayer : Int,
)

{
    var state:  GamePhase? = null
    var selected : MutableList<Card> = mutableListOf()
    var drawPile: Stack<Card> = Stack()
    var playStack: Stack<Card> = Stack()
    var lastRound: Boolean = false
    val log: MutableList<String> = mutableListOf()
    var isKnockRound: Boolean = false
    var knockInitiatorIndex: Int = -1
    var knockTurnsRemaining: Int = 0
    var showStartingCards: Boolean = false
}


