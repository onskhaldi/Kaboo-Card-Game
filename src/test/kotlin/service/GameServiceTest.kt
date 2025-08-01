
package service
import entity.*
import java.util.*
import kotlin.test.*
import org.junit.jupiter.api.assertThrows

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

    @Test
    fun testEndTurn_validEndTurn_advancesGameCorrectly() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.ENDTURN
        rootService.gameService.endTurn()
        assertEquals(GamePhase.READYTODRAW, game.state)
    }

    @Test
    fun testEndTurn_drawPileEmpty_gameEndsImmediately() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.ENDTURN
        game.drawPile.clear()

        rootService.gameService.endTurn()

        assertEquals(GamePhase.FINISHED, game.state) // evtl. direkt FINISHED, je nach gameOver()

    }

    @Test
    fun testEndTurn_whenKnocked_setsLastRoundTrue() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.KNOCKED

        rootService.gameService.endTurn()

        assertTrue(game.lastRound)
    }

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


    @Test
    fun startNewGame_throwsExceptionIfPlayerNameBlank() {
        val rootService = RootService()
        val gameService = rootService.gameService

        val exception = assertThrows<IllegalArgumentException> {
            gameService.startNewGame("Alice", "")
        }

        assertTrue(exception.message!!.contains("Spielername darf nicht leer sein."))
    }


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

    @Test
    fun startNewGame_distributesFourCardsPerPlayer() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")

        val game = rootService.currentGame!!

        assertEquals(4, game.player1.hand.flatten().filterNotNull().size)
        assertEquals(4, game.player2.hand.flatten().filterNotNull().size)
        assertEquals(52 - 8, game.drawPile.size) // 8 Karten wurden verteilt
    }
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

}
















