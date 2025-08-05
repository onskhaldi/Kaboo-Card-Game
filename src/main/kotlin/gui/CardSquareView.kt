package gui
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.gamecomponentviews.CardView

/**
 * A square grid container for displaying exactly four cards in a 2×2 layout.
 *
 * This class extends [GridPane] with fixed dimensions (2 rows × 2 columns) to provide
 * a visual representation of a player's hand or field in the game.
 * Each card is displayed with a fixed width and height, and the grid cells
 * can be cleared or filled dynamically.
 *
 * @constructor
 * Creates a new [CardSquareView] at the given position.
 * @param posX The X position of this grid in the scene.
 * @param posY The Y position of this grid in the scene.
 *
 * @see displayCards
 * @see clear
 */
class CardSquareView(posX: Number, posY: Number) :
    GridPane<CardView>(posX = posX, posY = posY, rows = 2, columns = 2) {

        private val cardWidth = 130.0
    private val cardHeight = 200.0
    /** The fixed width of each displayed card (in pixels). */
    /** The fixed height of each displayed card (in pixels). */
    init {
        width = 2 * cardWidth
        height = 2 * cardHeight

    }

    /**
     * Clears the grid by removing all card references from each cell.
     *
     * This sets every grid cell to `null`, effectively removing any
     * displayed [CardView] from the UI.
     */
    fun clear() {
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                this[col, row] = null
            }
        }
    }
    /**
     * Displays a list of cards in the grid.
     *
     * Cards are placed left-to-right, top-to-bottom in the grid.
     * If there are more than 4 cards, only the first 4 will be displayed.
     * Cards are resized to fit the fixed [cardWidth] and [cardHeight].
     *
     * @param cards The list of [CardView]s to display in the grid.
     */
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


