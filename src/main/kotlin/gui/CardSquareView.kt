package gui

import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.gamecomponentviews.CardView

class CardSquareView(posX: Number, posY: Number) :
    GridPane<CardView>(posX = posX, posY = posY, rows = 2, columns = 2) {

    private val cardWidth = 130.0
    private val cardHeight = 200.0

    init {
        width = 2 * cardWidth
        height = 2 * cardHeight

    }

    fun clear() {
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                this[col, row] = null
            }
        }
    }

    fun displayCards(cards: List<CardView>) {
        clear()
        cards.forEachIndexed { index, cardView ->
            cardView.apply {
                width = cardWidth
                height = cardHeight
            }

            val row = index / columns
            val col = index % columns
            if (row < rows && col < columns) {
                this[col, row] = cardView
            }
        }
    }
}

