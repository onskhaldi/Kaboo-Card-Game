package entity
import java.util.Stack
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

