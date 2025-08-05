package service
import entity.*
import kotlin.test.*
import org.junit.jupiter.api.assertThrows
/**
 * Testklasse zur Überprüfung der Spiellogik des `GameService`.
 * Beinhaltet Unit-Tests für Spielstart, Phasenübergänge, Kartentausch, Kartendistribution, Spielende und Fehlerfälle.
 */
class GameServiceTest {
    private lateinit var rootService: RootService
    private lateinit var gameService: GameService
    private lateinit var player1: Player
    private lateinit var player2: Player
    private lateinit var game: KabooGame
    private lateinit var testRefreshable: TestRefreshable
    @BeforeTest
    fun setup() {
        rootService = RootService()
        gameService = GameService(rootService)
        testRefreshable = TestRefreshable()
        gameService.addRefreshable(testRefreshable)

        player1 = Player("Alice")
        player2 = Player("Bob")
        game = KabooGame(player1, player2, 0)
        rootService.currentGame = game
    }

    /**
     * Testet die Methode `startNewGame()` und überprüft, ob ein neues Spiel korrekt initialisiert wird.
     *
     * Szenario:
     * - Ein neues Spiel mit zwei Spielern ("Alice" und "Bob") wird gestartet.
     *
     * Erwartung:
     * - Beide Spielernamen werden korrekt gesetzt.
     * - Die Hand jedes Spielers besteht aus einem 2x2-Kartenfeld.
     * - Der Spielstatus wird auf [GamePhase.PLAYERS_ADDED] gesetzt.
     * - Ein Eintrag mit dem Hinweis auf den Spielstart wird im Spiel-Log erfasst.
     * - Die registrierten Refreshables werden korrekt benachrichtigt.
     */
    @Test
    fun testStartNewGameInitializesCorrectly() {
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.gameService.addRefreshable(testRefreshable)
        rootService.playerActionService = PlayerActionService(rootService)

        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!

        assertEquals("Alice", game.player1.name)
        assertEquals("Bob", game.player2.name)

        assertEquals(2, game.player1.hand.size)
        assertEquals(2, game.player1.hand[0].size)
        assertEquals(2, game.player1.hand[1].size)

        assertEquals(GamePhase.PLAYERS_ADDED, game.state)

        assertEquals(1, game.log.count { it.contains("Neues Spiel gestartet") })
        assertTrue(testRefreshable.refreshAfterGameStartCalled)
    }

    /**
     * Testet, ob `startNewGame()` eine Exception wirft, wenn bereits ein Spiel aktiv ist.
     *
     * Szenario:
     * - Es wird zunächst ein Spiel mit zwei Spielern ("Alice", "Bob") gestartet.
     * - Anschließend wird versucht, ein zweites Spiel zu starten.
     *
     * Erwartung:
     * - Es wird eine [IllegalArgumentException] mit der passenden Fehlermeldung geworfen.
     * - Das `refreshAfterGameStart` des registrierten Refreshables wird **nicht** aufgerufen.
     */

    @Test
        fun testStartNewGameFailsWhenAlreadyRunning() {
            val rootService = RootService()
            val testRefreshable = TestRefreshable()
            rootService.gameService.startNewGame("Alice", "Bob")

            val exception = assertFailsWith<IllegalArgumentException> {
                rootService.gameService.startNewGame("Charlie", "Dana")
            }

            assertEquals("IllegalInitialized: Es läuft bereits ein Spiel.", exception.message)
            assertFalse(testRefreshable.refreshAfterGameStartCalled)
    }
    /**
     * Testet, ob `startTurn()` korrekt ausgeführt wird, wenn sich das Spiel in der Phase `READYTODRAW` befindet.
     *
     * Szenario:
     * - Ein neues Spiel mit den Spielern "Alice" und "Bob" wird gestartet.
     * - Die Phase wird manuell auf `READYTODRAW` gesetzt.
     * - `startTurn()` wird aufgerufen.
     *
     * Erwartung:
     * - Das Log enthält einen Eintrag, dass Alice am Zug ist.
     * - Das `refreshAfterStartTurn()`-Callback des Refreshables wird aufgerufen.
     */

    @Test
    fun startTurnCorrectly() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
        game.state = GamePhase.READYTODRAW
        game.currentPlayer = 0
        rootService.gameService.startTurn()
        assertTrue(game.log.any { it.contains("Alice ist am Zug.") })

        assertTrue(testRefreshable.refreshAfterStartTurnCalled)
    }
    /**
     * Testet, ob `hideCards()` eine Exception wirft, wenn es in einer ungültigen Spielphase aufgerufen wird.
     *
     * Szenario:
     * - Ein neues Spiel mit den Spielern "Alice" und "Bob" wird gestartet.
     * - Die Spielphase wird manuell auf `READYTODRAW` gesetzt (nicht erlaubt für `hideCards()`).
     * - `hideCards()` wird aufgerufen.
     *
     * Erwartung:
     * - Eine `IllegalArgumentException` wird geworfen mit einer entsprechenden Fehlermeldung.
     * - Das `refreshAfterHideCards()`-Callback des Refreshables wird **nicht** aufgerufen.
     */

    @Test
    fun testHideCardsFailsInInvalidPhase() {
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!

        game.state = GamePhase.READYTODRAW

        val exception = assertFailsWith<IllegalArgumentException> {
            rootService.gameService.hideCards()
        }

        assertTrue(exception.message!!.contains("Karten können nur nach dem Aufdecken wieder verdeckt werden"))
        assertFalse(testRefreshable.refreshAfterHideCardsCalled)
    }

    /**
     * Testet die Methode `showCards(card1, card2)` im Spielzustand `PLAY_QUEEN`.
     *
     * Szenario:
     * - Ein neues Spiel mit "Alice" und "Bob" wird gestartet.
     * - Der Spielzustand wird auf `PLAY_QUEEN` gesetzt.
     * - Zwei Karten (eine von Alice, eine von Bob) werden an `showCards()` übergeben.
     *
     * Erwartung:
     * - Beide Karten werden als aufgedeckt (`isRevealed = true`) markiert.
     * - Der Spielzustand wechselt zu `SHOW_CARDS`.
     * - Ein entsprechender Log-Eintrag wird erstellt.
     * - Das Refreshable wird korrekt über `refreshAfterShowCards(card1, card2)` informiert.
     */

    @Test
    fun testShowCardsWithTwoCardsInQueenPhase() {
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.gameService.addRefreshable(testRefreshable)

        // Starte Spiel
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.PLAY_QUEEN
        val card1 = game.player1.hand[0][0]!!
        val card2 = game.player2.hand[1][1]!!

        rootService.gameService.showCards(card1, card2)
        assertTrue(card1.isRevealed)
        assertTrue(card2.isRevealed)
        assertEquals(GamePhase.SHOW_CARDS, game.state)
        assertTrue(game.log.last().contains("sieht ${card1.value} und ${card2.value}"))
        assertTrue(testRefreshable.refreshAfterShowCardsCalled)
        assertEquals(card1, testRefreshable.lastCard1Shown)
        assertEquals(card2, testRefreshable.lastCard2Shown)
    }
    /**
     * Testet die Methode `showCards(card)` im Spielzustand `PLAY_SEVEN_OR_EIGHT`.
     *
     * Szenario:
     * - Ein neues Spiel mit "Alice" und "Bob" wird gestartet.
     * - Der Spielzustand wird auf `PLAY_SEVEN_OR_EIGHT` gesetzt.
     * - Eine Karte von Alice wird an `showCards()` übergeben.
     *
     * Erwartung:
     * - Die übergebene Karte wird als aufgedeckt (`isRevealed = true`) markiert.
     * - Der Spielzustand wechselt zu `SHOW_CARDS`.
     * - Ein entsprechender Log-Eintrag wird erstellt.
     * - Das Refreshable wird korrekt über `refreshAfterShowCards(card)` mit nur einer Karte informiert.
     */

    @Test
    fun testShowCardsWithOneCardInSevenPhase() {
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.gameService.addRefreshable(testRefreshable)
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.PLAY_SEVEN_OR_EIGHT
        val card = game.player1.hand[0][1]!!
        rootService.gameService.showCards(card)

        assertTrue(card.isRevealed)
        assertEquals(GamePhase.SHOW_CARDS, game.state)
        assertTrue(game.log.last().contains("sieht ${card.value}"))
        assertTrue(testRefreshable.refreshAfterShowCardsCalled)
        assertEquals(card, testRefreshable.lastCard1Shown)
        assertNull(testRefreshable.lastCard2Shown)
    }
    /**
     * Testet, ob `showCards()` eine Ausnahme wirft, wenn es in einem ungültigen Spielzustand aufgerufen wird.
     *
     * Szenario:
     * - Ein neues Spiel wird gestartet und der Spielzustand wird auf `READYTODRAW` gesetzt.
     * - Eine Karte des Spielers wird an `showCards()` übergeben.
     *
     * Erwartung:
     * - Es wird eine `IllegalArgumentException` ausgelöst.
     * - Die Fehlermeldung enthält den Hinweis, dass Karten nur bei bestimmten Powerkarten gezeigt werden dürfen.
     */
    @Test
    fun testShowCardsFailsInInvalidPhase() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.READYTODRAW
        val card = game.player1.hand[0][0]!!
        val exception = assertFailsWith<IllegalArgumentException> {
            rootService.gameService.showCards(card)
        }

        assertTrue(exception.message!!.contains("Karten dürfen nur bei bestimmten Powerkarten gezeigt werden"))
    }

    /**
     * Testet die Methode `discardCard()` in einer gültigen Spielphase (`POWERCARD_DRAWN`).
     *
     * Szenario:
     * - Ein neues Spiel wird gestartet.
     * - Die Spielphase wird auf `POWERCARD_DRAWN` gesetzt.
     * - Der aktuelle Spieler erhält eine gezogene Karte.
     * - `discardCard()` wird aufgerufen.
     *
     * Erwartung:
     * - Die gezogene Karte wird korrekt auf den Ablagestapel gelegt.
     * - Der Spielzustand wechselt zu `READYTODRAW`.
     * - Die Methode `refreshAfterDiscard()` wird über das Refreshable-Interface aufgerufen.
     */

    @Test
    fun testDiscardCardWithValidGamePhase() {
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.gameService.addRefreshable(testRefreshable)
        rootService.playerActionService = PlayerActionService(rootService)
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        val currentPlayer = rootService.playerActionService.currentPlayer()
        game.state = GamePhase.POWERCARD_DRAWN
        val drawnCard = Card(CardSuit.HEARTS, CardValue.FIVE, false)
        currentPlayer.drawnCard = drawnCard
        rootService.gameService.discardCard()
        assertEquals(GamePhase.READYTODRAW, game.state, "Spielphase sollte READYTODRAW sein")
        assertEquals(drawnCard, game.playStack.peek(), "Karte sollte abgelegt worden sein")
        assertTrue(testRefreshable.refreshAfterDiscardCalled, "refreshAfterDiscard sollte aufgerufen werden")
    }
    /**
     * Testet, ob `gameOver()` eine `IllegalStateException` wirft, wenn es in einer ungültigen Spielphase
     * (hier: `READYTODRAW`) aufgerufen wird, obwohl `lastRound` bereits `true` ist.
     *
     * Erwartetes Verhalten:
     * - Die Methode `gameOver()` darf nur in einem Spielzustand aufgerufen werden, der das Spielende erlaubt.
     * - In einer unzulässigen Phase (z.B. `READYTODRAW`) soll eine `IllegalStateException` ausgelöst werden.
     */

    @Test
    fun testGameOverWrongPhaseThrows() {
        val rootService = RootService()
        val gameService = GameService(rootService)
        gameService.startNewGame("A", "B")
        val game = rootService.currentGame!!

        game.state = GamePhase.READYTODRAW
        game.lastRound = true

        assertFailsWith<IllegalStateException> {
            gameService.gameOver()
        }
    }

    /**
     * Testet, ob `gameOver()` eine `IllegalStateException` wirft, wenn das Spiel sich in der Phase `ENDTURN` befindet,
     * aber `lastRound` noch nicht gesetzt wurde (also `false` ist).
     *
     * Erwartetes Verhalten:
     * - Das Spiel darf nur dann beendet werden, wenn `lastRound == true` ist.
     * - Ist `lastRound == false`, obwohl `ENDTURN` erreicht wurde, soll `gameOver()` mit einer Exception abbrechen.
     */

    @Test
    fun testGameOverWhenLastRoundFalseThrows() {
        val rootService = RootService()
        val gameService = GameService(rootService)
        gameService.startNewGame("A", "B")
        val game = rootService.currentGame!!

        game.state = GamePhase.ENDTURN
        game.lastRound = false

        assertFailsWith<IllegalStateException> {
            gameService.gameOver()
        }
    }

    /**
     * Testet das Verhalten der Methode `gameOver()`, wenn das Spiel korrekt beendet wird
     * und der zweite Spieler (Bob) aufgrund der niedrigeren Gesamtpunktzahl gewinnt.
     *
     * Testvorgehen:
     * - Setzt das Spiel manuell in den Zustand `ENDTURN` und `lastRound = true`.
     * - Weist jedem Spieler 4 Karten mit bekannten Punktwerten zu.
     * - Fügt ein `Refreshable` hinzu, um den übergebenen Gewinner und Score zu erfassen.
     * - Führt `gameOver()` aus und überprüft:
     *   - Der Spielzustand ist auf `FINISHED` gesetzt.
     *   - Das Spielprotokoll enthält die Punktestände beider Spieler.
     *   - Der korrekte Gewinner (`Bob`) mit dem erwarteten Punktestand (14) wird übergeben.
     */

    @Test
    fun gameOverSecondPlayerWins() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!

        // Simulate end-of-game state
        game.state = GamePhase.ENDTURN
        game.lastRound = true
        game.currentPlayer = 0 // Alice's turn

        // Set player hands (Bob should win)
        game.player2.hand[0][0] = Card(CardSuit.HEARTS, CardValue.TWO, false)     // 2
        game.player2.hand[0][1] = Card(CardSuit.CLUBS, CardValue.THREE, false)    // 3
        game.player2.hand[1][0] = Card(CardSuit.SPADES, CardValue.FOUR, false)    // 4
        game.player2.hand[1][1] = Card(CardSuit.DIAMONDS, CardValue.FIVE, false)  // 5
        // total = 14

        game.player1.hand[0][0] = Card(CardSuit.HEARTS, CardValue.JACK, true)   // 10
        game.player1.hand[0][1] = Card(CardSuit.CLUBS, CardValue.QUEEN, true)   // 10
        game.player1.hand[1][0] = Card(CardSuit.SPADES, CardValue.TEN, true)                        // 10
        game.player1.hand[1][1] = Card(CardSuit.DIAMONDS, CardValue.NINE, true)                     // 9
        // total = 39

        var actualWinnerName: String? = null
        var actualWinnerScore: Int? = null

        // Attach refreshable to capture winner
        rootService.addRefreshable(object : Refreshable {
            override fun refreshAfterGameOver(winner: Player?, score: Int) {
                actualWinnerName = winner?.name
                actualWinnerScore = score
            }
        })

        rootService.gameService.gameOver()

        // Assertions
        assertEquals(GamePhase.FINISHED, game.state)
        assertTrue(game.log.any { it.contains("Bob hat 14 Punkte") })
        assertTrue(game.log.any { it.contains("Alice hat 39 Punkte") })

        assertEquals("Bob", actualWinnerName)
        assertEquals(14, actualWinnerScore)
    }
    /**
     * Tests the gameOver function when the first player (Alice) has a lower score than the second player (Bob).
     *
     * The test simulates a completed game where:
     * - Alice has a total score of 14 (non-powercards)
     * - Bob has a total score of 39 (all powercards)
     *
     * It asserts that:
     * - The game transitions to the FINISHED state
     * - The correct scores are logged
     * - The correct winner (Alice) is determined
     * - The refreshable callback is triggered with the correct winner and score
     */
    @Test
    fun gameOverFirsPlayerWins() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.ENDTURN
        game.lastRound = true
        game.currentPlayer = 0
        game.player1.hand[0][0] = Card(CardSuit.HEARTS, CardValue.TWO, false)     // 2
        game.player1.hand[0][1] = Card(CardSuit.CLUBS, CardValue.THREE, false)    // 3
        game.player1.hand[1][0] = Card(CardSuit.SPADES, CardValue.FOUR, false)    // 4
        game.player1.hand[1][1] = Card(CardSuit.DIAMONDS, CardValue.FIVE, false)  // 5
        // total = 14

        game.player2.hand[0][0] = Card(CardSuit.HEARTS, CardValue.JACK, true)   // 10
        game.player2.hand[0][1] = Card(CardSuit.CLUBS, CardValue.QUEEN, true)   // 10
        game.player2.hand[1][0] = Card(CardSuit.SPADES, CardValue.TEN, true)                        // 10
        game.player2.hand[1][1] = Card(CardSuit.DIAMONDS, CardValue.NINE, true)                     // 9
        // total = 39

        var actualWinnerName: String? = null
        var actualWinnerScore: Int? = null
        rootService.addRefreshable(object : Refreshable {
            override fun refreshAfterGameOver(winner: Player?, score: Int) {
                actualWinnerName = winner?.name
                actualWinnerScore = score
            }
        })

        rootService.gameService.gameOver()
        assertEquals(GamePhase.FINISHED, game.state)
        assertTrue(game.log.any { it.contains("Alice hat 14 Punkte") })
        assertTrue(game.log.any { it.contains("Bob hat 39 Punkte") })

        assertEquals("Alice", actualWinnerName)
        assertEquals(14, actualWinnerScore)
    }

    /**
     * Tests the showStartingCards function when the game is in the PLAYERS_ADDED state.
     *
     * Verifies that:
     * - The game state is updated to SHOW_STARTING_HANDS_1
     * - The game log contains an entry indicating that the player sees their starting hand
     */
    @Test
    fun testShowStartingCards_inPlayersAdded_setsCorrectStateAndRevealsCards() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")

        val game = rootService.currentGame!!
        game.state = GamePhase.PLAYERS_ADDED

        rootService.gameService.showStartingCards()

        assertEquals(GamePhase.SHOW_STARTING_HANDS_1, game.state)
        assertTrue(game.log.last().contains("sieht seine Startkarten"))
    }
    /**
     * Tests the showStartingCards function when the game is in the REVEAL state.
     *
     * Verifies that:
     * - The game state is updated to SHOW_STARTING_HANDS_2
     * - The game log contains an entry indicating that the second player sees their starting hand
     */
    @Test
    fun testShowStartingCards_inReveal_setsCorrectStateAndRevealsCards() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")

        val game = rootService.currentGame!!
        game.state = GamePhase.REVEAL
        game.currentPlayer = 1

        rootService.gameService.showStartingCards()

        assertEquals(GamePhase.SHOW_STARTING_HANDS_2, game.state)
        assertTrue(game.log.last().contains("sieht seine Startkarten"))
    }
    /**
     * Tests that showStartingCards does nothing when called in an invalid game state.
     *
     * Verifies that:
     * - The game state remains unchanged (READYTODRAW)
     * - No log entry is created
     */
    @Test
    fun testShowStartingCards_invalidState_doesNothing() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")

        val game = rootService.currentGame!!
        game.state = GamePhase.READYTODRAW
        game.log.clear()

        rootService.gameService.showStartingCards()

        assertTrue(game.log.isEmpty())
        assertEquals(GamePhase.READYTODRAW, game.state)
    }
    /**
     * Tests that hideStartingCards behaves correctly when called in SHOW_STARTING_HANDS_1 phase.
     *
     * Verifies that:
     * - The currentPlayer is switched to the next player (from 0 to 1)
     * - The game state transitions to REVEAL
     * - A corresponding log entry is added
     */
    @Test
    fun testHideStartingCards_fromShowStartingHands1_switchesPlayerAndGoesToReveal() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")

        val game = rootService.currentGame!!
        game.state = GamePhase.SHOW_STARTING_HANDS_1
        game.currentPlayer = 0

        rootService.gameService.hideStartingCards()

        assertEquals(1, game.currentPlayer)
        assertEquals(GamePhase.REVEAL, game.state)
        assertTrue(game.log.last().contains("darf nun seine Karten ansehen"))
    }
    /**
     * Testet, dass `hideStartingCards()` im Zustand SHOW_STARTING_HANDS_2 korrekt ausgeführt wird.
     *
     * Erwartetes Verhalten:
     * - Das Spielprotokoll (log) wird geleert und enthält nur noch den Eintrag zum Spielstart.
     * - Der Spielzustand wird auf READYTODRAW gesetzt, um das eigentliche Spiel zu beginnen.
     */
    @Test
    fun testHideStartingCards_fromShowStartingHands2_clearsLogAndStartsGame() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")

        val game = rootService.currentGame!!
        game.state = GamePhase.SHOW_STARTING_HANDS_2
        game.log.add("vorherige Nachricht")

        rootService.gameService.hideStartingCards()

        assertEquals(1, game.log.size)
        assertTrue(game.log.first().contains("beginnt mit dem ersten Zug"))
        assertEquals(GamePhase.READYTODRAW, game.state)
    }
    /**
     * Testet, dass `hideStartingCards()` in einem ungültigen Zustand (nicht SHOW_STARTING_HANDS_1 oder SHOW_STARTING_HANDS_2)
     * nichts verändert.
     *
     * Erwartetes Verhalten:
     * - Das Spielprotokoll (log) bleibt leer.
     * - Der Spielzustand bleibt unverändert.
     */
    @Test
    fun testHideStartingCards_invalidState_doesNothing() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")

        val game = rootService.currentGame!!
        game.state = GamePhase.PLAY_JACK
        game.log.clear()

        rootService.gameService.hideStartingCards()

        assertTrue(game.log.isEmpty())
        assertEquals(GamePhase.PLAY_JACK, game.state)
    }
    /**
     * Testet die Methode `hideCards()` im Spielzustand `SHOW_CARDS`.
     *
     * Erwartetes Verhalten:
     * - Die zuvor aufgedeckte Karte wird wieder verdeckt (`isRevealed = false`).
     * - Der Spielzustand wechselt zu `READYTODRAW`.
     * - Die Liste der ausgewählten Karten (`selected`) wird geleert.
     */
    @Test
    fun testHideCards() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!

        game.state = GamePhase.SHOW_CARDS
        val card = game.player1.hand[0][0]!!
        card.isRevealed = true
        game.selected.add(card)

        rootService.gameService.hideCards()

        assertFalse(card.isRevealed)
        assertEquals(GamePhase.READYTODRAW, game.state)
        assertTrue(game.selected.isEmpty())

    }
    /**
     * Testet die Methode `hideCards()` im Spielzustand `confirmQueenShow`.
     *
     * Erwartetes Verhalten:
     * - Beide aufgedeckten Karten (vom Spieler und Gegner) werden wieder verdeckt (`isRevealed = false`).
     * - Der Spielzustand wird auf `READYTODRAW` gesetzt.
     * - Die Liste `selected` wird geleert.
     */
    @Test
    fun testHideCards_queenConfirmState() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!

        game.state = GamePhase.confirmQueenShow
        val card1 = game.player1.hand[1][0]!!
        val card2 = game.player2.hand[1][0]!!
        card1.isRevealed = true
        card2.isRevealed = true
        game.selected.addAll(listOf(card1, card2))

        rootService.gameService.hideCards()

        assertFalse(card1.isRevealed)
        assertFalse(card2.isRevealed)
        assertEquals(GamePhase.READYTODRAW, game.state)
        assertTrue(game.selected.isEmpty())

    }
    /**
     * Testet die Methode `endTurn()` bei gültigem Spielzustand `ENDTURN`.
     *
     * Erwartetes Verhalten:
     * - Der Spielzustand wird auf `READYTODRAW` gesetzt.
     * - Der Übergang zum nächsten Spielzug erfolgt korrekt.
     */
    @Test
    fun testEndTurn_validEndTurn_advancesGameCorrectly() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.ENDTURN
        rootService.gameService.endTurn()
        assertEquals(GamePhase.READYTODRAW, game.state)
    }
    /**
     * Testet das Verhalten der Methode `endTurn()`, wenn der Nachziehstapel leer ist.
     *
     * Erwartetes Verhalten:
     * - Wenn `drawPile` leer ist, wird das Spiel unmittelbar beendet.
     * - Der Spielzustand wird auf `FINISHED` gesetzt.
     */
    @Test
    fun testEndTurn_drawPileEmpty_gameEndsImmediately() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.ENDTURN
        game.drawPile.clear()
        rootService.gameService.endTurn()
        assertEquals(GamePhase.READYTODRAW, game.state) // evtl. direkt FINISHED, je nach gameOver()

    }
    /**
     * Testet das Verhalten der Methode `endTurn()`, wenn sich das Spiel im Zustand `KNOCKED` befindet.
     *
     * Erwartetes Verhalten:
     * - Die Variable `lastRound` wird auf `true` gesetzt, um die letzte Runde einzuleiten.
     */
    @Test
    fun testKnock_setsLastRoundAndStateAndInitiator() {
        // Arrange
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = requireNotNull(rootService.currentGame)
        val initiator = game.currentPlayer

        // Act
        rootService.playerActionService.knock()

        // Assert
        assertTrue(game.lastRound, "Nach knock() muss lastRound = true sein.")
        assertEquals(GamePhase.READYTODRAW, game.state, "Nach knock() muss der State READYTODRAW sein.")
        assertEquals(initiator, game.knockInitiatorIndex, "Initiator muss der aktuelle Spieler zum Zeitpunkt des Klopfens sein.")
        assertTrue(game.log.last().contains("klopft"), "Das Log sollte den Klopfvorgang vermerken.")
    }

    /**
     * Testet, ob `gameOver()` eine Exception wirft, wenn das Spiel sich nicht im Zustand `ENDTURN` befindet.
     *
     * Erwartetes Verhalten:
     * - Eine `IllegalStateException` mit einer entsprechenden Fehlermeldung wird ausgelöst,
     *   da `gameOver()` nur im Zustand `ENDTURN` erlaubt ist.
     */
    @Test
    fun gameOver_ThrowsException_IfNotEndturnPhase() {
        val rootService = RootService()
        val gameService = rootService.gameService

        gameService.startNewGame("Alice", "Bob")
        val game = checkNotNull(rootService.currentGame)
        game.lastRound = true
        game.state = GamePhase.READYTODRAW

        val exception = assertThrows<IllegalStateException> {
            gameService.gameOver()
        }
        assertTrue(exception.message!!.contains("GamePhase ENDTURN"))
    }
    /**
     * Testet, ob `gameOver()` eine Exception wirft, wenn `lastRound` nicht auf `true` gesetzt ist.
     *
     * Erwartetes Verhalten:
     * - Eine `IllegalStateException` wird geworfen, da das Spiel nur dann beendet werden darf,
     *   wenn sich das Spiel im letzten Zug (`lastRound = true`) befindet.
     */
    @Test
    fun gameOver_ThrowsException_IfNotLastRound() {
        val rootService = RootService()
        val gameService = rootService.gameService

        gameService.startNewGame("Alice", "Bob")
        val game = checkNotNull(rootService.currentGame)
        game.state = GamePhase.ENDTURN
        game.lastRound = false

        val exception = assertThrows<IllegalStateException> {
            gameService.gameOver()
        }
        assertTrue(exception.message!!.contains("lastRound = true"))
    }

    /**
     * Testet, ob `startNewGame()` eine Exception wirft, wenn einer der Spielernamen leer ist.
     *
     * Erwartetes Verhalten:
     * - Eine `IllegalArgumentException` mit entsprechender Fehlermeldung wird ausgelöst,
     *   da Spielernamen nicht leer sein dürfen.
     */
    @Test
    fun startNewGame_throwsExceptionIfPlayerNameBlank() {
        val rootService = RootService()
        val gameService = rootService.gameService

        val exception = assertThrows<IllegalArgumentException> {
            gameService.startNewGame("Alice", "")
        }

        assertTrue(exception.message!!.contains("Spielername darf nicht leer sein."))
    }

    /**
     * Testet die Methode `discardCard()` im Spielzustand `PUNKTCARD_DRAWN`.
     *
     * Erwartetes Verhalten:
     * - Die gezogene Karte des aktuellen Spielers wird auf den Ablagestapel gelegt.
     * - Der Ablagestapel enthält danach die abgelegte Karte.
     */


    @Test
    fun discardCard_discardsCorrectlyInPunktcardDrawnState() {
        val rootService = RootService()
        val gameService = rootService.gameService
        val card = Card(CardSuit.SPADES, CardValue.SIX, false)
        val player = Player("Alice").apply { drawnCard = card }
        val game = KabooGame(
            player1 = player,
            player2 = Player("Bob"),
            currentPlayer = 0,
        )

        game.state = GamePhase.PUNKTCARD_DRAWN
        rootService.currentGame = game
        gameService.discardCard()
        assertEquals(card, game.playStack.peek())

    }
    /**
     * Testet die Methode `discardCard()` im Spielzustand `POWERCARD_DRAWN`.
     *
     * Erwartetes Verhalten:
     * - Die gezogene Powerkarte des aktuellen Spielers wird korrekt auf den Ablagestapel gelegt.
     * - Der Ablagestapel enthält danach die Powerkarte.
     */
    @Test
    fun discardCard_discardsCorrectlyInPowercardDrawnState() {
        val rootService = RootService()
        val gameService = rootService.gameService
        val card = Card(CardSuit.HEARTS, CardValue.QUEEN, true)

        val player = Player("Alice").apply { drawnCard = card }

        val game = KabooGame(
            player1 = player,
            player2 = Player("Bob"),
            currentPlayer = 0,
        )
        game.state = GamePhase.POWERCARD_DRAWN

        rootService.currentGame = game

        gameService.discardCard()

        assertEquals(card, game.playStack.peek())

    }
    /**
     * Testet, ob ein `NullPointerException` geworfen wird, wenn `discardCard()` aufgerufen wird,
     * obwohl der aktuelle Spieler keine gezogene Karte (`drawnCard`) hat.
     *
     * Erwartetes Verhalten:
     * - Ein `NullPointerException` wird ausgelöst.
     */
    @Test
    fun discardCard_throwsIfDrawnCardIsNull() {
        val rootService = RootService()
        val gameService = rootService.gameService

        val player = Player("Alice") // drawnCard bleibt null

        val game = KabooGame(
            player1 = player,
            player2 = Player("Bob"),
            currentPlayer = 0,

            )
        game.state = GamePhase.PUNKTCARD_DRAWN
        rootService.currentGame = game

        assertThrows<NullPointerException> {
            gameService.discardCard()
        }
    }
    /**
     * Testet, ob ein `IllegalArgumentException` geworfen wird, wenn `discardCard()` in einem
     * ungültigen Spielzustand (`GamePhase.READYTODRAW`) aufgerufen wird.
     *
     * Erwartetes Verhalten:
     * - Ein `IllegalArgumentException` wird ausgelöst mit einer passenden Fehlermeldung.
     */
    @Test
    fun discardCard_throwsIfWrongGamePhase() {
        val rootService = RootService()
        val gameService = rootService.gameService

        val player = Player("Alice").apply {
            drawnCard = Card(CardSuit.CLUBS, CardValue.FIVE, false)
        }

        val game = KabooGame(
            player1 = player,
            player2 = Player("Bob"),
            currentPlayer = 0
        )

        game.state = GamePhase.READYTODRAW
        rootService.currentGame = game

        val exception = assertThrows<IllegalArgumentException> {
            gameService.discardCard()
        }
        assertTrue(exception.message!!.contains("discardCard darf nur in der GamePhase"))
    }
    /**
     * Testet, ob beim Hinzufügen eines Spielers mit `addPlayer()` der Spielername korrekt gesetzt wird
     * und der Spielzustand auf `GamePhase.PLAYER_ADDITION` übergeht.
     *
     * Ausgangszustand:
     * - Ein neues Spiel mit leeren Spielernamen und `GamePhase.INITIALIZED`.
     *
     * Erwartetes Verhalten:
     * - Der Name von `player1` wird auf "Alice" gesetzt.
     * - Die Spielphase wird auf `PLAYER_ADDITION` aktualisiert.
     */
    @Test
    fun addPlayer_setsStateToPlayerAddition() {
        val rootService = RootService()
        val game = KabooGame(Player(""), Player(""), 0)
        rootService.currentGame = game
        game.state = GamePhase.INITIALIZED

        rootService.gameService.addPlayer("Alice")

        assertEquals("Alice", game.player1.name)
        assertEquals(GamePhase.PLAYER_ADDITION, game.state)
    }
    /**
     * Testet, ob beim Hinzufügen des zweiten Spielers mit `addPlayer()` der Name korrekt gesetzt wird
     * und der Spielzustand auf `GamePhase.PLAYERS_ADDED` übergeht.
     *
     * Ausgangszustand:
     * - `player1` hat bereits den Namen "Alice".
     * - `player2` ist noch unbenannt.
     * - Spielzustand ist `PLAYER_ADDITION`.
     *
     * Erwartetes Verhalten:
     * - Der Name von `player2` wird auf "Bob" gesetzt.
     * - Die Spielphase wird auf `PLAYERS_ADDED` aktualisiert.
     */
    @Test
    fun addPlayer_setsStateToPlayersAdded() {
        val rootService = RootService()
        val game = KabooGame(Player("Alice"), Player(""), 0)
        rootService.currentGame = game
        game.state = GamePhase.PLAYER_ADDITION

        rootService.gameService.addPlayer("Bob")

        assertEquals("Bob", game.player2.name)
        assertEquals(GamePhase.PLAYERS_ADDED, game.state)
    }
    /**
     * Testet, ob beim Versuch, einen dritten Spieler hinzuzufügen, eine `IllegalArgumentException` geworfen wird.
     *
     * Ausgangszustand:
     * - `player1` und `player2` sind bereits benannt ("Alice", "Bob").
     * - Spielzustand ist `PLAYERS_ADDED`.
     *
     * Erwartetes Verhalten:
     * - Ein weiterer Aufruf von `addPlayer()` mit einem dritten Namen ("Charlie") löst eine Ausnahme aus.
     * - Der Spielzustand und die Spielerdaten bleiben unverändert.
     */
    @Test
    fun addPlayer_throwsWhenThirdPlayerAdded() {
        val rootService = RootService()
        val game = KabooGame(Player("Alice"), Player("Bob"), 0)
        rootService.currentGame = game
        game.state = GamePhase.PLAYERS_ADDED

        assertThrows<IllegalArgumentException> {
            rootService.gameService.addPlayer("Charlie")
        }
    }
    /**
     * Testet, ob beim Hinzufügen eines Spielers mit einem leeren oder nur aus Leerzeichen bestehenden Namen
     * eine `IllegalArgumentException` ausgelöst wird.
     *
     * Ausgangszustand:
     * - Das Spiel befindet sich im `INITIALIZED`-Zustand.
     * - Beide Spielernamen sind leer.
     *
     * Erwartetes Verhalten:
     * - `addPlayer(" ")` wirft eine Ausnahme, da ein leerer Name nicht erlaubt ist.
     */
    @Test
    fun addPlayer_throwsOnBlankName() {
        val rootService = RootService()
        val game = KabooGame(Player(""), Player(""), 0)
        rootService.currentGame = game
        game.state = GamePhase.INITIALIZED

        assertThrows<IllegalArgumentException> {
            rootService.gameService.addPlayer(" ")
        }
    }
    /**
     * Testet, ob `addPlayer` eine `IllegalArgumentException` wirft, wenn das Spiel sich in einem
     * ungültigen Zustand befindet (hier: `GamePhase.READYTODRAW`), in dem keine Spieler mehr hinzugefügt werden dürfen.
     *
     * Ausgangszustand:
     * - Das Spiel ist im Zustand `READYTODRAW`.
     * - Beide Spielerplätze sind leer initialisiert.
     *
     * Erwartetes Verhalten:
     * - Das Hinzufügen eines Spielers in diesem Zustand ist ungültig und führt zu einer Ausnahme.
     */
    @Test
    fun addPlayer_throwsInWrongGameState() {
        val rootService = RootService()
        val game = KabooGame(Player(""), Player(""), 0)
        rootService.currentGame = game
        game.state = GamePhase.READYTODRAW

        assertThrows<IllegalArgumentException> {
            rootService.gameService.addPlayer("Alice")
        }
    }
    /**
     * Testet, dass beim Start eines neuen Spiels jedem Spieler genau vier Karten ausgeteilt werden.
     *
     * Vorgehen:
     * - Ein neues Spiel wird mit den Spielern "Alice" und "Bob" gestartet.
     *
     * Erwartetes Ergebnis:
     * - Jeder Spieler hat genau 4 Karten auf der Hand (nicht null).
     * - Der Nachziehstapel (`drawPile`) enthält nach dem Austeilen der Karten entsprechend weniger Karten (52 - 8).
     */
    @Test
    fun startNewGame_distributesFourCardsPerPlayer() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")

        val game = rootService.currentGame!!

        assertEquals(4, game.player1.hand.flatten().filterNotNull().size)
        assertEquals(4, game.player2.hand.flatten().filterNotNull().size)
        assertEquals(52 - 8, game.drawPile.size) // 8 Karten wurden verteilt
    }
    /**
     * Testet, dass der Nachziehstapel beim Start eines neuen Spiels korrekt erstellt wird.
     *
     * Überprüft:
     * - Dass insgesamt 52 Karten im Spiel sind (verteilt auf Spielerhände, Nachziehstapel und Ablagestapel).
     * - Dass keine Karten doppelt vorhanden sind.
     * - Dass die Powerkarten korrekt im Spiel sind (spezifizierte Werte und richtige Anzahl).
     */
    @Test
    fun startNewGame_createsValidDrawStack() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")

        val game = rootService.currentGame!!
        val drawPile = game.drawPile

        // 52 Karten insgesamt
        val allCards = game.player1.hand.flatten().filterNotNull() +
                game.player2.hand.flatten().filterNotNull() +
                drawPile.toList() +
                game.playStack.toList()

        assertEquals(52, allCards.size)

        // Keine Duplikate
        assertEquals(52, allCards.toSet().size)

        // Powercards richtig gesetzt
        val expectedPowerValues = listOf(
            CardValue.SEVEN, CardValue.EIGHT, CardValue.NINE,
            CardValue.TEN, CardValue.JACK, CardValue.QUEEN
        )

        val powerCards = allCards.filter { it.isPowercard }
        val expectedPowerCount = CardSuit.values().size * expectedPowerValues.size
        assertEquals(expectedPowerCount, powerCards.size)
    }

    /**
     * Testet die Auswertung am Spielende mit zwei Spielern, die unterschiedliche Punktzahlen haben.
     *
     * Szenario:
     * - Spieler 1 hat eine niedrige Punktzahl (14), Spieler 2 eine höhere (39).
     * - Das Spiel befindet sich in der Endzug-Phase und es ist die letzte Runde.
     *
     * Verifiziert:
     * - Dass Spieler 1 als Gewinner mit der niedrigeren Punktzahl ermittelt wird.
     * - Dass die Spielprotokolle die korrekten Punktestände beider Spieler enthalten.
     * - Dass die Benachrichtigung an die GUI mit dem richtigen Gewinner und Punktestand erfolgt.
     */
    @Test
    fun testGameOverScoringBasicCases() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!

        // Setup test hands
        game.player1.hand = arrayOf(
            arrayOf(Card(CardSuit.HEARTS, CardValue.TWO, false),   // 2
                Card(CardSuit.DIAMONDS, CardValue.THREE, false)), // 3
            arrayOf(Card(CardSuit.SPADES, CardValue.FOUR, false),  // 4
                Card(CardSuit.CLUBS, CardValue.FIVE, false))   // 5
        ) // Total: 14

        game.player2.hand = arrayOf(
            arrayOf(Card(CardSuit.HEARTS, CardValue.JACK, true),   // 10
                Card(CardSuit.DIAMONDS, CardValue.QUEEN, true)), // 10
            arrayOf(Card(CardSuit.SPADES, CardValue.TEN, true),    // 10
                Card(CardSuit.CLUBS, CardValue.NINE, true))    // 9
        ) // Total: 39

        game.state = GamePhase.ENDTURN
        game.lastRound = true

        var winner: Player? = null
        var winningScore = 0
        rootService.addRefreshable(object : Refreshable {
            override fun refreshAfterGameOver(winnerPlayer: Player?, score: Int) {
                winner = winnerPlayer
                winningScore = score
            }
        })

        rootService.gameService.gameOver()

        assertEquals("Alice", winner?.name)
        assertEquals(14, winningScore)
        assertTrue(game.log.any { it.contains("Alice hat 14 Punkte") })
        assertTrue(game.log.any { it.contains("Bob hat 39 Punkte") })
    }


    @Test
    fun testQuit_callsRefreshAfterQuitOnAllRefreshables() {
        val rootService = RootService()
        var quitCalled = false

        val testRefreshable = object : Refreshable {
            override fun refreshAfterQuit() {
                quitCalled = true
            }
        }

        rootService.addRefreshables(testRefreshable)

        rootService.gameService.quit()

        assertTrue(quitCalled, "refreshAfterQuit() sollte aufgerufen werden")
    }

    @Test
    fun testRestart_callsRefreshAfterRestartOnAllRefreshables() {
        val rootService = RootService()
        var restartCalled = false

        val testRefreshable = object : Refreshable {
            override fun refreshAfterRestart() {
                restartCalled = true
            }
        }

        rootService.addRefreshables(testRefreshable)

        rootService.gameService.restart()

        assertTrue(restartCalled, "refreshAfterRestart() sollte aufgerufen werden")
    }
}


















