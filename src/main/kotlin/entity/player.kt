package entity
/**
 * Represents a player in the game "Kaboo".
 *
 * Each player has a [name], a 2×2 [hand] of face-down or revealed cards,
 * and optionally a [drawnCard] that is currently held in hand temporarily.
 *
 * @property name The name of the player.
 * @property hand A 2x2 grid of cards representing the player's hand. Cards may be null initially.
 * @property drawnCard A card drawn by the player during their turn, or null if no card is currently drawn.
 */
class Player(val name: String) {
    /**
     * The 2×2 grid representing the player's hand.
     *
     * Each cell may contain a card or be null (especially at game start).
     * Cards can be face-down or revealed depending on the game phase and actions.
     *
     * `hand[0]` = top row (usually hidden), `hand[1]` = bottom row (can be revealed).
     */
    var hand: Array<Array<Card?>> = Array(2) { Array<Card?>(2) { null } }

    /**
     * A card that the player has drawn during their turn and is temporarily holding.
     *
     * May be null if the player has not drawn a card or has already played/discarded it.
     */
    var drawnCard: Card? = null

    /**
     * A list of the player's starting cards that have been revealed.
     *
     * Typically contains the two bottom-row cards after the initial reveal phase.
     */
    var startingCards: MutableList<Card> = mutableListOf()

    /**
     * Returns a string representation of the player,
     * including the number of cards currently in hand.
     */
    override fun toString(): String {
        val cardsInHand = hand.flatten().count { it != null }
        return "$name: Hand$cardsInHand Cards"
    }
}

