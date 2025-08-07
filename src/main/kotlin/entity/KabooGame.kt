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
    /**
     * The current phase of the game (e.g., DRAW, DISCARD, etc.).
     */
    var state: GamePhase? = null

    /**
     * List of currently selected cards (used during actions like swapping or revealing).
     */
    var selected: MutableList<Card> = mutableListOf()

    /**
     * Stack of cards available to be drawn by players.
     */
    var drawPile: Stack<Card> = Stack()

    /**
     * Stack of cards discarded by players.
     */
    var playStack: Stack<Card> = Stack()

    /**
     * Indicates whether the last round of the game has started.
     * This is triggered after a knock action.
     */
    var lastRound: Boolean = false

    /**
     * A chronological log of actions and events during the game.
     * Each entry is a string describing an action.
     */
    val log: MutableList<String> = mutableListOf()

    /**
     * Index of the player who initiated the knock (0 or 1).
     * Used to determine when the game should end.
     */
    var knockInitiatorIndex: Int = -1

    /**
     * Flag indicating whether players should currently reveal their starting cards.
     */
    var showStartingCards: Boolean = false
}

