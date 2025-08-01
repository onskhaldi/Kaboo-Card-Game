package entity
/**
 * Data class for the single typ of game elements that the game "Kombi-Duell" knows: cards
 *
 * It is characterized by a [CardSuit] and a [CardValue]
 *
 * @param suit the suit of the card
 * @param value the value of the card
 * @property suit the suit of the card
 * @property value the value of the card
 */

data class Card(
    val suit: CardSuit,
    val value: CardValue,
    var isPowercard: Boolean,
    var isRevealed: Boolean = false

)
{
    fun isPowerCard(): Boolean {
        return value == CardValue.SEVEN ||
                value == CardValue.EIGHT ||
                value == CardValue.NINE ||
                value == CardValue.TEN ||
                value == CardValue.JACK ||
                value == CardValue.QUEEN
    }

    override fun toString() = "$suit$value"

}

