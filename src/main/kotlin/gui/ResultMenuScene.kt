package gui
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Color
import service.RootService
import entity.*
import service.Refreshable
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

class ResultMenuScene(private val rootService: RootService) : MenuScene(400, 1080), Refreshable {

    private val headlineLabel = Label(
        width = 300, height = 50, posX = 50, posY = 50,
        text = "Game Over",
        font = Font(size = 22)
    )

    private val p1Score = Label(
        width = 300,
        height = 40,
        posX = 50,
        posY = 120,
        text = "",
        font = Font(size = 20, fontWeight = Font.FontWeight.MEDIUM)
    ).apply {
        visual = ColorVisual(220, 220, 220) // light gray but darker than before
    }

    private val p2Score = Label(
        width = 300,
        height = 40,
        posX = 50,
        posY = 170,
        text = "",
        font = Font(size = 20, fontWeight = Font.FontWeight.MEDIUM)
    ).apply {
        visual = ColorVisual(220, 220, 220)
    }

    private val gameResult = Label(
        width = 300,
        height = 50,
        posX = 50,
        posY = 230,
        text = "",
        font = Font(size = 22, fontWeight = Font.FontWeight.BOLD)
    ).apply {
        visual = ColorVisual(255, 255, 200) // soft yellow for high contrast
    }



    val quitButton = Button(width = 140, height = 35, posX = 50, posY = 265, text = "Quit").apply {
        visual = ColorVisual(Color(221,136,136))
        onMouseClicked={ visual = ColorVisual(Color(221,136,136))
            rootService.gameService.quit()}
    }

    val newGameButton = Button(width = 140, height = 35, posX = 210, posY = 265, text = "New Game").apply {
        visual = ColorVisual(Color(136, 221, 136))
        onMouseClicked={ColorVisual(Color(136, 221, 136))
            rootService.gameService.restart()}
    }

    init {
        opacity = 0.5
        addComponents(headlineLabel,
            p1Score,
            p2Score,
            gameResult,
            newGameButton,
            quitButton)
    }
    override fun refreshAfterGameOver(gewinner: Player?, score: Int) {
        val game = rootService.currentGame ?: return
        val s1 = rootService.gameService.scoreOf(game.player1)
        val s2 = rootService.gameService.scoreOf(game.player2)

        p1Score.text = "${game.player1.name}: $s1 Punkte"
        p2Score.text = "${game.player2.name}: $s2 Punkte"

        gameResult.text = when {
            gewinner == null -> "Unentschieden!"
            gewinner == game.player1 -> " ${game.player1.name} gewinnt!"
            else -> " ${game.player2.name} gewinnt!"
        }

    }

}


