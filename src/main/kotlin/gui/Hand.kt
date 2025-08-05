
package gui

import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView


class Hand(posX: Number, posY: Number):
    LinearLayout<CardView>(spacing = 4.0, posX = posX, posY = posY)
{
    init {
        width = 800.0
        height = 200.0
    }
}