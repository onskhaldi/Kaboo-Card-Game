package gui
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import service.Refreshable
import service.RootService
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
/**
 * Menu scene that is displayed at the start of the application or when restarting the game.
 *
 * This scene allows two players to enter their names and start a new game, or quit the application.
 * It includes:
 * - Two labeled text fields for player names.
 * - A "Start" button to initialize the game.
 * - A "Quit" button to close the application.
 *
 * @param rootService Reference to the [RootService] that manages the game state and services.
 */
class NewGameMenuScene(private val rootService: RootService) : MenuScene(400, 1080), Refreshable {
    /** Headline label for the scene. */
    private val headlineLabel = Label(
        width = 300, height = 50, posX = 50, posY = 50,
        text = "Start New Game",
        font = Font(size = 22)
    )
    /** Label for Player 1's name input field. */
    val p1Label = Label(
        width = 100, height = 35,
        posX = 50, posY = 125,
        text = "Player 1:"
    )

    /**
     * Text field for Player 1's name.
     * Defaults to a random Simpsons character.
     * Automatically enables/disables the start button depending on input.
     */
    val p1Input: TextField = TextField(
        width = 200, height = 35,
        posX = 150, posY = 125,
        text = listOf("Homer", "Marge", "Bart", "Lisa", "Maggie").random()
    ).apply {
        onKeyPressed = {
            startButton.isDisabled = this.text.isBlank() || p2Input.text.isBlank()
        }
    }
    /** Label for Player 2's name input field. */
    val p2Label = Label(
        width = 100, height = 35,
        posX = 50, posY = 170,
        text = "Player 2:"
    )

    /**
     * Text field for Player 2's name.
     * Defaults to a random Futurama character.
     * Automatically enables/disables the start button depending on input.
     */

    val p2Input: TextField = TextField(
        width = 200, height = 35,
        posX = 150, posY = 170,
        text = listOf("Fry", "Bender", "Leela", "Amy", "Zoidberg").random()
    ).apply {
        onKeyPressed = {
            startButton.isDisabled = p1Input.text.isBlank() || this.text.isBlank()
        }
    }
    /** Button to quit the application. */
    val quitButton = Button(
        width = 140, height = 35,
        posX = 50, posY = 240,
        text = "Quit"
    ).apply {
        visual = ColorVisual(221, 136, 136)
    }
    /**
     * Button to start the game.
     * When clicked, it uses the names from the text fields to start a new game via [RootService].
     */
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

