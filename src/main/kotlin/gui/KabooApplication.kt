
package gui
import entity.Player
import tools.aqua.bgw.core.BoardGameApplication
import service.Refreshable
import service.RootService
class KabooApplication : BoardGameApplication("Kaboo"), Refreshable {

    private val rootService = RootService()

    private val gameScene = GameScene(rootService)

    private val resultMenuScene = ResultMenuScene(rootService).apply {
        newGameButton.onMouseClicked = {

            rootService.gameService.restart()
            this@KabooApplication.showMenuScene(newGameMenuScene)
        }
        quitButton.onMouseClicked = { exit() }
    }

    private val newGameMenuScene = NewGameMenuScene(rootService).apply {
        startButton.onMouseClicked = {
            if (p1Input.text.isNotBlank() && p2Input.text.isNotBlank()) {
                rootService.gameService.startNewGame(
                    p1Input.text.trim(),
                    p2Input.text.trim()
                )
                this@KabooApplication.hideMenuScene()
                this@KabooApplication.showGameScene(gameScene)
            }
        }
        quitButton.onMouseClicked = { exit() }
    }

    init {

        rootService.addRefreshables(
            this,
            gameScene,
            resultMenuScene,
            newGameMenuScene
        )

        showMenuScene(newGameMenuScene, 0)
    }

    override fun refreshAfterStartNewGame() {
        hideMenuScene()
    }

    override fun refreshAfterGameOver(winner: Player?, score: Int) {

        showMenuScene(resultMenuScene)
    }


    override fun refreshAfterStartTurn() { hideMenuScene() }

    override fun refreshAfterTurnEnd() {
        val nextPlayerScene = NextPlayerScene(rootService)
        showMenuScene(nextPlayerScene)
    }

    override fun refreshAfterQuit() { exit() }

    override fun refreshAfterRestart() { showMenuScene(newGameMenuScene) }
}


