package service
import kotlin.random.Random
import entity.*
import java.util.Stack
/**
 * Service layer class that provides the logic for actions not directly
 * related to a single player.
 *
 * @param rootService The [RootService] instance to access the other service methods and entity layer
 */

class GameService (private val rootService: RootService): AbstractRefreshingService() {
    /**
     * Startet ein neues Spiel mit den übergebenen Spielernamen.
     * Initialisiert Spieler, Kartenstapel und Spielfeld.
     *
     * @param p1Name Name von Spieler 1
     * @param p2Name Name von Spieler 2
     * @throws IllegalArgumentException wenn bereits ein Spiel läuft oder Spielernamen ungültig sind
     */

    fun startNewGame(p1Name: String, p2Name: String) {
        require(rootService.currentGame == null) {
            "IllegalInitialized: Es läuft bereits ein Spiel."
        }

        val starterIndex = Random.nextInt(0, 2)
        val game = KabooGame(
            player1 = Player(name = ""),
            player2 = Player(name = ""),
            currentPlayer = starterIndex
        )
        rootService.currentGame = game
        game.state = GamePhase.INITIALIZED
        addPlayer(p1Name)
        addPlayer(p2Name)

        require(game.state == GamePhase.PLAYERS_ADDED) {
            "Spieler NICHT hinzugefügt werden."
        }

        val drawStack = createDrawStack()
        distributeCards(drawStack, game)
        game.drawPile = drawStack
        game.playStack.clear()

        game.log.add("Neues Spiel gestartet. ${rootService.playerActionService.currentPlayer().name} beginnt.")

        onAllRefreshables {
            refreshAfterStartNewGame()
        }
    }

    /**
     * Fügt einen Spieler zum aktuellen Spiel hinzu.
     * Nur erlaubt in den Phasen INITIALIZED oder PLAYER_ADDITION.
     *
     * @param name Spielername (nicht leer)
     * @throws IllegalStateException wenn bereits zwei Spieler existieren
     * @throws IllegalArgumentException wenn Spielphase oder Name ungültig sind
     */
    fun addPlayer(name: String) {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active" }
        require(game.state == GamePhase.INITIALIZED || game.state == GamePhase.PLAYER_ADDITION) {
            "Spieler können nur in der Initialisierungsphase hinzugefügt werden."
        }
        require(name.isNotBlank()) { "Spielername darf nicht leer sein." }

        val player = Player(name)

        when {
            game.player1.name.isBlank() -> {
                game.player1 = player
                game.state = GamePhase.PLAYER_ADDITION // First player added
            }

            game.player2.name.isBlank() -> {
                game.player2 = player
                game.state = GamePhase.PLAYERS_ADDED // Both players added
            }
            else -> throw IllegalStateException("Bereits zwei Spieler vorhanden.")
        }
        game.log.add("Spieler $name wurde hinzugefügt.")
        onAllRefreshables { refreshAfterPlayerEdit() }
    }


    /**
     * Zeigt die unteren Karten eines Spielers zu Beginn des Spiels.
     * Ändert den Zustand zu SHOW_STARTING_HANDS_1 oder SHOW_STARTING_HANDS_2.
     */

    fun showStartingCards() {
        val game = rootService.currentGame ?: return
        game.showStartingCards = true

        when (game.state) {
            GamePhase.PLAYERS_ADDED -> {
                game.log.add("${rootService.playerActionService.currentPlayer().name} sieht seine Startkarten.")
                revealBottomCards(game, rootService.playerActionService.currentPlayer())
            }

            GamePhase.REVEAL -> {
                game.log.add("${rootService.playerActionService.currentPlayer().name} sieht seine Startkarten.")
                revealBottomCards(game, rootService.playerActionService.currentPlayer())
            }
            else -> return }
    }


    /**
     * Versteckt die Karten nach dem Anfangszeigen.
     * Setzt Spielphase auf REVEAL oder READYTODRAW.
     */

    fun hideStartingCards() {
        val game = rootService.currentGame ?: return
        game.showStartingCards = false

        when (game.state) {
            GamePhase.SHOW_STARTING_HANDS_1 -> {
                game.currentPlayer = if (game.currentPlayer == 0) 1 else 0
                game.state = GamePhase.REVEAL
                game.log.add("${rootService.playerActionService.currentPlayer().name} darf nun seine Karten ansehen.")
            }

            GamePhase.SHOW_STARTING_HANDS_2 -> {
                game.log.clear()
                game.currentPlayer = Random.nextInt(0, 2)
                game.log.add("${rootService.playerActionService.currentPlayer().name} beginnt mit dem ersten Zug.")
                game.state = GamePhase.READYTODRAW
                //startTurn()
            }
            else -> return
        }
        onAllRefreshables {refreshAfterHideStartingCards()}
    }

    /**
     * Deckt die beiden unteren Karten eines Spielers auf.
     * Wird intern von showStartingCards() genutzt.
     *
     * @param game Das aktuelle Spiel
     * @param player Der Spieler, dessen Karten gezeigt werden
     */


    private  fun revealBottomCards(game: KabooGame, player: Player) {
        player.startingCards = mutableListOf(player.hand[1][0]!!, player.hand[1][1]!!)

        if (game.state==GamePhase.PLAYERS_ADDED) {
            game.state = GamePhase.SHOW_STARTING_HANDS_1
        }
        else if (game.state==GamePhase.REVEAL){
            game.state = GamePhase.SHOW_STARTING_HANDS_2
        }
        onAllRefreshables {
            refreshAfterShowStartingCards()
        }
    }



    /**
     * Versteckt aufgedeckte Karten während spezieller Spielphasen
     * Beendet ggf. den Zug.
     *
     * @throws IllegalStateException wenn die Phase ungültig ist
     */
    fun hideCards() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active." }

        require(game.state == GamePhase.SHOW_CARDS|| game.state == GamePhase.CONFIRMQUEENSHOW) {
            "Karten können nur nach dem Aufdecken wieder verdeckt werden. Aktuelle Phase: ${game.state}"
        }

        for (card in game.selected) {
            card.isRevealed = false
        }
        val currentPlayer = if (game.currentPlayer == 0) game.player1 else game.player2
        game.log.add("${currentPlayer.name} hat die Karten wieder verdeckt.")
        game.selected.clear()
        onAllRefreshables { refreshAfterHideCards() }
        game.state = GamePhase.ENDTURN
        endTurn()
    }

    /**
     * Zeigt eine oder zwei Karten im Rahmen der Queen-/Seven/Eight-/Ten-Phasen.
     *
     * @param card1 erste Karte zum Aufdecken
     * @param card2 optionale zweite Karte
     * @throws IllegalStateException wenn falsche Spielphase
     */

    /*fun showCards(card1: Card, card2: Card? = null) {

        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active." }
        require(game.state in listOf(
            GamePhase.PLAY_QUEEN,
            GamePhase.PLAY_SEVEN_OR_EIGHT,
            GamePhase.PLAY_NINE_OR_TEN
        )) {
            "Karten dürfen nur bei bestimmten Powerkarten gezeigt werden. Aktuelle Phase: ${game.state}"
        }

        card1.isRevealed = true
        if (card2 != null) {
            card2.isRevealed = true
        }

        val currentPlayer = if (game.currentPlayer == 0) game.player1 else game.player2
        val card2Info = card2?.let { " und ${it.value}" } ?: ""
        game.log.add("${currentPlayer.name} sieht ${card1.value}$card2Info.")
        game.state = GamePhase.SHOW_CARDS
        onAllRefreshables { refreshAfterShowCards(card1,card2) }
    }*/



    fun showCards(card1: Card, card2: Card? = null) {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active." }
        require(game.state in listOf(
            GamePhase.PLAY_QUEEN,
            GamePhase.PLAY_SEVEN_OR_EIGHT,
            GamePhase.PLAY_NINE_OR_TEN
        )) {
            "Karten dürfen nur bei bestimmten Powerkarten gezeigt werden. Aktuelle Phase: ${game.state}"
        }

        card1.isRevealed = true


        if (card2 != null) {
            card2.isRevealed = true

        }

        val currentPlayer = if (game.currentPlayer == 0) game.player1 else game.player2
        val card2Info = card2?.let { " und ${it.value}" } ?: ""
        game.log.add("${currentPlayer.name} sieht ${card1.value}$card2Info.")
        game.state = GamePhase.SHOW_CARDS

        onAllRefreshables { refreshAfterShowCards(card1, card2) }
    }



    /**
     * Legt die gezogene Karte auf den Ablagestapel.
     * Erlaubt nur in den Phasen POWERCARD_DRAWN oder PUNKTCARD_DRAWN.
     * Ruft anschließend endTurn() auf.
     *
     * @throws IllegalStateException bei falscher Phase
     * @throws NullPointerException wenn keine gezogene Karte vorhanden ist
     */

    fun discardCard() {
        val game = rootService.currentGame
        checkNotNull(game) { "Kein aktives Spiel vorhanden." }
        require(game.state == GamePhase.POWERCARD_DRAWN || game.state == GamePhase.PUNKTCARD_DRAWN) {
            "discardCard darf nur in der GamePhase POWERCARD_DRAWN oder NORMALCARD_DRAWN aufgerufen werden." }
        val player =rootService.playerActionService.currentPlayer()
        val card =player.drawnCard!!

        game.playStack.push(card)
        game.log.add("${player.name} hat ${card.value} of ${card.suit} auf den Ablagestapel gelegt.")
        onAllRefreshables { refreshAfterDiscard() }
        game.state = GamePhase.ENDTURN
        endTurn()
    }
    /**
     * Startet den Zug des aktuellen Spielers.
     * Nur erlaubt in Phase READYTODRAW.
     */

    fun startTurn() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active" }
        require(game.currentPlayer == 0 || game.currentPlayer == 1)
        { "Ungültiger Spieler-Index: ${game.currentPlayer}" }
        require(game.state == GamePhase.READYTODRAW)
        { "startTurn() darf nur in der Phase DRAW_FROM_DECK aufgerufen werden (aktuelle Phase: ${game.state})" }
        game.log.add("Neuer Zug: ${rootService.playerActionService.currentPlayer().name} ist am Zug.")
        onAllRefreshables { refreshAfterStartTurn() }
    }


    /**
     * Beendet den aktuellen Spielzug und setzt ggf. das Spiel in den Endzustand.
     * Kümmert sich um Spielerwechsel, Knock-Logik, und ruft ggf. gameOver() auf.
     */

    fun endTurn() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active" }
        val player = rootService.playerActionService.currentPlayer()
        require(game.currentPlayer == 0 || game.currentPlayer == 1)
        { "Ungültiger Spieler-Index: ${game.currentPlayer}"  }

        require(game.state == GamePhase.ENDTURN || game.state == GamePhase.KNOCKED) {
            "endTurn darf nur in der Phase ENDTURN oder KNOCKED aufgerufen werden (aktuell: ${game.state})"
        }
        if (game.state == GamePhase.FINISHED) return
        if (game.drawPile.isEmpty()) {
            game.lastRound = true
            game.state = GamePhase.ENDTURN
            game.log.add("Der Nachziehstapel ist leer. Das Spiel endet sofort.")
            gameOver()  }

        if (game.lastRound && game.currentPlayer == game.knockInitiatorIndex) {
            game.log.add("Letzter Zug des Klopfenden. Spiel endet.")
            gameOver()
            return

        }
        game.currentPlayer = 1 - game.currentPlayer
        player.drawnCard = null
        game.selected.clear()
        game.log.add("Zug beendet. Jetzt ist $rootService.playerActionService.currentPlayer().name} am Zug.")
        game.state= GamePhase.READYTODRAW

        onAllRefreshables { refreshAfterTurnEnd() }
    }

    /**
     * Erstellt und mischt ein vollständiges 52-Karten-Deck.
     * Powerkarten werden entsprechend gekennzeichnet.
     *
     * @return Gemischter Kartenstapel als Stack
     */
    private fun createDrawStack():Stack<Card> {
        val allCards = CardSuit.values().flatMap { suit ->
            CardValue.values().map { value ->
                Card(
                    suit = suit,
                    value = value,
                    isPowercard = value in listOf(
                        CardValue.SEVEN, CardValue.EIGHT,
                        CardValue.NINE, CardValue.TEN,
                        CardValue.JACK, CardValue.QUEEN
                    )
                )
            }
        }.shuffled()

        val stack = Stack<Card>()
        allCards.forEach { stack.push(it) }
        return stack
    }
    /**
     * Verteilt an jeden Spieler 2×2 Karten aus dem Deck.
     *
     * @param deck Der gemischte Kartenstapel
     * @param game Das aktuelle Spiel
     */
    private fun distributeCards(deck: Stack<Card>, game: KabooGame) {
        val players = listOf(game.player1, game.player2)
        for (player in players) {
            for (row in 0..1) {
                for (col in 0..1) {
                    player.hand[row][col] = deck.pop()
                }
            }
        }
    }


    /**
     * Berechnet den Gesamtpunktestand eines Spielers basierend auf seiner Hand.
     *
     * @param player Der Spieler
     * @return Gesamtpunktzahl des Spielers
     */
    fun scoreOf(player: Player): Int {
        return player.hand.flatten().filterNotNull().sumOf { card ->
            when (card.value) {
                CardValue.KING -> -1
                CardValue.ACE -> 1
                CardValue.JACK, CardValue.QUEEN -> 10
                else -> card.value.ordinal + 2 // 2–10: ordinal(0–8) → +1
            }
        }
    }

    /**
     * Führt das Spielende durch: berechnet Punkte, bestimmt Gewinner
     * und benachrichtigt die GUI über das Ergebnis.
     *
     * @throws IllegalStateException wenn falscher Zustand oder Spiel nicht in letzter Runde ist
     */
    fun gameOver() {
        val game = rootService.currentGame ?: throw IllegalStateException("Kein aktives Spiel vorhanden.")
        if (game.state != GamePhase.ENDTURN) {
            throw IllegalStateException("getScore darf nur in der GamePhase ENDTURN aufgerufen werden.")
        }
        if (!game.lastRound) {
            throw IllegalStateException("getScore darf nur aufgerufen werden, wenn lastRound = true ist.")
        }

        val player = if (game.currentPlayer == 0) game.player1 else game.player2
        val gegner = if (game.currentPlayer == 0) game.player2 else game.player1

        val scorePlayer1 = scoreOf(player)
        val scorePlayer2 = scoreOf(gegner)
        game.state = GamePhase.FINISHED
        game.log.add("${player.name} hat ${scorePlayer1.toString()} Punkte.")
        game.log.add("${game.player2.name} hat ${scorePlayer2.toString()} Punkte.")
        when {
            scorePlayer1 < scorePlayer2 -> {
                onAllRefreshables {
                    refreshAfterGameOver(player, scorePlayer1)
                }
            }

            scorePlayer1 > scorePlayer2 -> {
                onAllRefreshables {
                    refreshAfterGameOver(gegner, scorePlayer2)
                }
            }

        } }
    /**
     * Ends the current game session and clears the game state.
     *
     * Sets [rootService.currentGame] to `null` to indicate that no game is active,
     * then notifies all registered refreshables via [refreshAfterQuit] so the UI
     * and other listeners can update accordingly.
     */
    fun quit() {
        rootService.currentGame = null
        onAllRefreshables { refreshAfterQuit() }
    }
    /**
     * Restarts the game by clearing the current game state.
     *
     * Similar to [quit], sets [rootService.currentGame] to `null`,
     * but triggers [refreshAfterRestart] so the UI and logic can
     * reinitialize for a new game session.
     */
    fun restart() {
        rootService.currentGame = null
        onAllRefreshables { refreshAfterRestart() }
    }

}






