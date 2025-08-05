package gui
import tools.aqua.bgw.components.container.CardStack
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.TextVisual
/**
 * A visual representation of a stack of cards with an optional label.
 *
 * This component extends [CardStack] to display cards in a stack formation,
 * and adds a semi-transparent background with a label text overlay for identification
 * (e.g., "Draw Stack" or "Discard Pile").
 *
 * @param posX The horizontal position of the stack in the scene.
 * @param posY The vertical position of the stack in the scene.
 * @param label The text label to display on top of the stack (default is empty).
 * @param rotate If `true`, rotates the visual by 180 degrees to show an upside-down stack
 *               (useful for opponents' draw or discard piles).
 */
class LabeledStackView(posX: Number, posY: Number, label: String = "", rotate: Boolean = false) :
    CardStack<CardView>(height = 200, width = 130, posX = posX, posY = posY) {
        init {
        visual = CompoundVisual(
            ColorVisual(Color(255, 255, 255, 50)),
            TextVisual(label)
        ).apply {
            if (rotate) rotation = 180.0
        }
    }
}


