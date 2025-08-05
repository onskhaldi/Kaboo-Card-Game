package gui

import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.MenuScene
import service.RootService
import service.Refreshable
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * A [MenuScene] that is displayed between player turns to inform the next player
 * that it is their turn and allow them to start it.
 *
 * The scene displays:
 * - The next player's name with a "get ready" message.
 * - A button to begin the turn.
 *
 * This scene appears after [service.Refreshable.refreshAfterTurnEnd] is called.
 *
 * @param rootService Reference to the [RootService] that manages the game state and turn flow.
 */

class NextPlayerScene(private val rootService: RootService) : MenuScene(800, 500) , Refreshable {
    val game = requireNotNull(rootService.currentGame) { "No game is currently active." }
    /** The player whose turn is about to start. */
    val currPlayer = when (game.currentPlayer) {
        0-> game.player1
        1 -> game.player2
        else -> throw IllegalStateException(
            "Invalid current player index: ${game.currentPlayer}")
    }
    /**
     * Button that the player clicks to start their turn.
     * Styled with a green background and large rounded corners.
     */
    private val startTurnButton = Button(
        width = 220,
        height = 80,
        posX = 290,
        posY = 320,
        text = "▶ It's Your Turn",
        font = Font(size = 26, fontWeight = Font.FontWeight.BOLD)
    ).apply {
        visual = ColorVisual(50, 205, 50).apply { // LimeGreen
            style.borderRadius = BorderRadius.LARGE
        }
        onMouseClicked = { rootService.gameService.startTurn() }
    }
    /**
     * Label displaying the current player's name and a "get ready" message.
     * Styled with a white background and medium rounded corners.
     */
    private val playerNameLabel = Label(
        width = 500,
        height = 80,
        posX = 150,
        posY = 150,
        text = " ${currPlayer.name}, get ready!",
        font = Font(size = 34, fontWeight = Font.FontWeight.BOLD)
    ).apply {
        visual = ColorVisual(255, 255, 255).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }
    }
    init {
        opacity = 0.8
        background = ColorVisual(30, 30, 30)
        addComponents(playerNameLabel, startTurnButton)
    }
}



