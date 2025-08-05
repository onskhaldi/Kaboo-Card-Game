package gui
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import service.Refreshable
import service.RootService
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

class NewGameMenuScene(private val rootService: RootService) : MenuScene(400, 1080), Refreshable {

    private val headlineLabel = Label(
        width = 300, height = 50, posX = 50, posY = 50,
        text = "Start New Game",
        font = Font(size = 22)
    )

    val p1Label = Label(
        width = 100, height = 35,
        posX = 50, posY = 125,
        text = "Player 1:"
    )


    val p1Input: TextField = TextField(
        width = 200, height = 35,
        posX = 150, posY = 125,
        text = listOf("Homer", "Marge", "Bart", "Lisa", "Maggie").random()
    ).apply {
        onKeyPressed = {
            startButton.isDisabled = this.text.isBlank() || p2Input.text.isBlank()
        }
    }

    val p2Label = Label(
        width = 100, height = 35,
        posX = 50, posY = 170,
        text = "Player 2:"
    )


    val p2Input: TextField = TextField(
        width = 200, height = 35,
        posX = 150, posY = 170,
        text = listOf("Fry", "Bender", "Leela", "Amy", "Zoidberg").random()
    ).apply {
        onKeyPressed = {
            startButton.isDisabled = p1Input.text.isBlank() || this.text.isBlank()
        }
    }

    val quitButton = Button(
        width = 140, height = 35,
        posX = 50, posY = 240,
        text = "Quit"
    ).apply {
        visual = ColorVisual(221, 136, 136)
    }

    val startButton = Button(
        width = 140, height = 35,
        posX = 210, posY = 240,
        text = "Start"
    ).apply {
        visual = ColorVisual(136, 221, 136)
        onMouseClicked = {
            rootService.gameService.startNewGame(
                p1Input.text.trim(),
                p2Input.text.trim()
            )
        }
    }

    init {
        opacity = .5
        addComponents(
            headlineLabel,
            p1Label, p1Input,
            p2Label, p2Input,
            startButton, quitButton
        )
    }
}

