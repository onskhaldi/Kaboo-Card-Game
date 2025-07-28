package entity
/**
 * Entity class that represents a game state of "kabooGame". As all card stack information is stored
 * in the [Player] entity, this class is just a wrapper for two player objects.
 *
 * @param player1 The first player
 * @param player2 The second player
 */
data class KabooGame (
    var player1: Player,
    var player2: Player,
    var currentPlayer : Int,
)

{ var state:  GamePhase? = null
    var selected : MutableList<Card> = mutableListOf()
    var drawPile: MutableList<Card> = mutableListOf()
    var playStack: MutableList<Card> = mutableListOf()
    var lastRound: Boolean = false
    val log: MutableList<String> = mutableListOf()
}