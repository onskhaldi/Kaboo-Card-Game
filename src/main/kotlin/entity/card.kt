package entity
/**
 * Data class for the single typ of game elements that the game "KabooGame" knows: cards.
 *
 * It is characterized by a [CardSuit] a [CardValue] and an [isPowercard]
 *
 * @property suit the suit of the card
 * @property value the value of the card
 */

data class Card(
    val suit: CardSuit,
    val value: CardValue,
    var isPowercard: Boolean,
)
{override fun toString() = "$suit$value"}