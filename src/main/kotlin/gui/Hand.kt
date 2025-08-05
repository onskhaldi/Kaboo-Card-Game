package gui
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
/**
 * A visual container representing a player's hand of cards in the Kaboo game.
 *
 * This class is a [LinearLayout] specifically configured to hold [CardView] elements,
 * with a fixed size and spacing between cards.
 *
 * @constructor
 * Creates a new [Hand] positioned at the given coordinates.
 *
 * @param posX The horizontal position of the hand in the scene.
 * @param posY The vertical position of the hand in the scene.
 */
class Hand(posX: Number, posY: Number):
    LinearLayout<CardView>(spacing = 4.0, posX = posX, posY = posY)
{
    init {
        width = 800.0
        height = 200.0
    }
}


