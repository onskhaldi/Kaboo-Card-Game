package gui

import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.MenuScene
import service.RootService
import service.Refreshable
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual


class NextPlayerScene(private val rootService: RootService) : MenuScene(800, 500) , Refreshable {
    val game = requireNotNull(rootService.currentGame) { "No game is currently active." }
    val currPlayer = when (game.currentPlayer) {
        0-> game.player1
        1 -> game.player2
        else -> throw IllegalStateException(
            "Invalid current player index: ${game.currentPlayer}")
    }
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

