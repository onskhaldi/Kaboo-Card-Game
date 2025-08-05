
package gui
import entity.Player
import tools.aqua.bgw.core.BoardGameApplication
import service.Refreshable
import service.RootService
/**
 * Main entry point for the Kaboo card game application.
 *
 * This class extends [BoardGameApplication] and manages the overall
 * scene transitions between the main game scene, new game menu, result menu,
 * and intermediate scenes like the next player prompt.
 *
 * It also implements [Refreshable] so it can react to state changes
 * from the [RootService] and update which scene is displayed.
 */
class KabooApplication : BoardGameApplication("Kaboo"), Refreshable {
    /** Central service that manages game state and logic. */
    private val rootService = RootService()
    /** Main scene where the actual game is played. */
    private val gameScene = GameScene(rootService)
    /**
     * Scene shown after the game ends, displaying the winner
     * and allowing the player to start a new game or quit.
     */
    private val resultMenuScene = ResultMenuScene(rootService).apply {
        newGameButton.onMouseClicked = {

            rootService.gameService.restart()
            this@KabooApplication.showMenuScene(newGameMenuScene)
        }
        quitButton.onMouseClicked = { exit() }
    }
    /**
     * Scene shown when starting the application or after a restart.
     * Allows players to input their names and begin a new game.
     */
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
    /**
     * Called when a new game has started.
     *
     * Hides any visible menu and leaves the game scene active.
     */

    override fun refreshAfterStartNewGame() {
        hideMenuScene()
    }
    /**
     * Called when the game has ended.
     *
     * Displays the [resultMenuScene] showing the winner and score.
     *
     * @param winner The winning player, or `null` in case of a draw.
     * @param score The final score difference or total, depending on scoring rules.
     */
    override fun refreshAfterGameOver(winner: Player?, score: Int) {

        showMenuScene(resultMenuScene)
    }
    /**
     * Called when a player's turn begins.
     *
     * Hides any active menu so the game scene is visible for play.
     */

    override fun refreshAfterStartTurn() { hideMenuScene() }
    /**
     * Called when a player's turn ends.
     *
     * Shows the [NextPlayerScene] to inform the next player that it is their turn.
     */
    override fun refreshAfterTurnEnd() {
        val nextPlayerScene = NextPlayerScene(rootService)
        showMenuScene(nextPlayerScene)
    }

    /**
     * Called when the game is quit.
     *
     * Exits the application entirely.
     */
    override fun refreshAfterQuit() { exit() }

    /**
     * Called when the game is restarted.
     *
     * Displays the [newGameMenuScene] to allow setting up a new match.
     */
    override fun refreshAfterRestart() { showMenuScene(newGameMenuScene) }
}



