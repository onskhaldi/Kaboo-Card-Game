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
    var hand: Array<Array<Card?>> = Array(2) { Array<Card?>(2) { null } }
    var drawnCard: Card? = null

    override fun toString(): String {
        val cardsInHand = hand.flatten().count { it != null }
        return "$name: Hand$cardsInHand Cards"
    }
}
