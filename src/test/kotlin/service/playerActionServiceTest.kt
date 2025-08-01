package service
import entity.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*
import java.lang.reflect.Method
import org.junit.jupiter.api.assertThrows

/**
 * Class that provides tests for [PlayerActionService]. [TestRefreshable] is used to validate
 * correct refreshing behavior even though no GUI is present.
 */
class PlayerActionServiceTest {
    /**
     * The [RootService] is initialized in the [setUpGame] function
     * hence it is a late-initialized property.
     */
    private lateinit var rootService: RootService
    private lateinit var playerActionService: PlayerActionService
    private lateinit var game: KabooGame
    private lateinit var refreshable: TestRefreshable


    @BeforeEach
    fun setup() {
        rootService = RootService()
        game = KabooGame(Player("Alice"), Player("Bob"), currentPlayer = 0)
        rootService.currentGame = game
        playerActionService = rootService.playerActionService
        refreshable = TestRefreshable()
        rootService.addRefreshable(refreshable)
    }


    @Test
    fun testDrawFromDeckwithPowerCard() {
        val game = rootService.currentGame!!
        val player = rootService.playerActionService.currentPlayer()

        val queen = Card(CardSuit.HEARTS, CardValue.QUEEN, true)
        game.drawPile.push(queen)

        game.state = GamePhase.READYTODRAW

        assertNull(player.drawnCard)

        rootService.playerActionService.drawFromDeck()

        assertEquals(queen, player.drawnCard)
        assertTrue(game.state == GamePhase.POWERCARD_DRAWN)
        assertTrue(refreshable.refreshAfterDrawCalled)
    }



    @Test
    fun testDrawFromDeckWithPointCard() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)

        val pointCard = Card(CardSuit.SPADES, CardValue.FIVE, false)
        game.drawPile.add(pointCard)

        game.state = GamePhase.READYTODRAW
        game.currentPlayer = 0

        rootService.playerActionService.drawFromDeck()

        assertEquals(pointCard, game.player1.drawnCard)

        assertEquals(GamePhase.PUNKTCARD_DRAWN, game.state)

        assertTrue(game.log.last().contains("hat eine Punktekarte (${pointCard.value})"))

        assertTrue(testRefreshable.refreshAfterDrawCalled)
    }

    @Test
    fun testPlayPowerCard_Queen() {
        val rootService = RootService()
        val game = KabooGame(
            player1 = Player("Alice"),
            player2 = Player("Bob"),
            currentPlayer = 0
        )
        rootService.currentGame = game

        val queen = Card(CardSuit.HEARTS, CardValue.QUEEN,true)
        game.player1.drawnCard = queen
        game.state = GamePhase.POWERCARD_DRAWN

        rootService.playerActionService.playPowerCard()

        assertEquals(GamePhase.PLAY_QUEEN, game.state)

        assertTrue(game.log.last().contains("sieht mit QUEEN"))

        assertEquals(queen, game.playStack.peek())
    }



    @Test
    fun testPlayPowerCard_Jack() {
        val rootService = RootService()
        val player = Player("Alice")
        val opponent = Player("Bob")
        val jack = Card(CardSuit.SPADES, CardValue.JACK, isPowercard = true)

        player.drawnCard = jack

        val game = KabooGame(
            player1 = player,
            player2 = opponent,
            currentPlayer = 0
        ).apply {
            state = GamePhase.POWERCARD_DRAWN

        }

        rootService.currentGame = game
        rootService.playerActionService = PlayerActionService(rootService)

        rootService.playerActionService.playPowerCard()

        assertEquals(GamePhase.PLAY_JACK, game.state)
        assertTrue(game.playStack.peek() == jack)
        assertTrue(game.log.last().contains("Bube"))
    }

    @Test
    fun testPlayPowerCard_Seven() {
        val rootService = RootService()
        val player = Player("Alice")
        val opponent = Player("Bob")
        val seven = Card(CardSuit.CLUBS, CardValue.SEVEN, isPowercard = true)

        player.drawnCard = seven

        val game = KabooGame(
            player1 = player,
            player2 = opponent,
            currentPlayer = 0
        ).apply {
            state = GamePhase.POWERCARD_DRAWN

        }

        rootService.currentGame = game
        rootService.playerActionService = PlayerActionService(rootService)

        rootService.playerActionService.playPowerCard()

        assertEquals(GamePhase.PLAY_SEVEN_OR_EIGHT, game.state)
        assertTrue(game.log.last().contains("eigene Karte ansehen"))
    }


    @Test
    fun testPlayPowerCard_Nine() {
        val rootService = RootService()
        val player = Player("Alice")
        val opponent = Player("Bob")
        val nine = Card(CardSuit.DIAMONDS, CardValue.NINE, isPowercard = true)

        player.drawnCard = nine

        val game = KabooGame(
            player1 = player,
            player2 = opponent,
            currentPlayer = 0
        ).apply {
            state = GamePhase.POWERCARD_DRAWN

        }
        rootService.currentGame = game
        rootService.playerActionService = PlayerActionService(rootService)

        rootService.playerActionService.playPowerCard()

        assertEquals(GamePhase.PLAY_NINE_OR_TEN, game.state)
        assertTrue(game.log.last().contains("eine Karte von"))
    }

    @Test
    fun testPlayPowerCard_Eight() {
        val rootService = RootService()
        val player = Player("Alice")
        val opponent = Player("Bob")
        val eight = Card(CardSuit.HEARTS, CardValue.EIGHT, isPowercard = true)

        player.drawnCard = eight

        val game = KabooGame(
            player1 = player,
            player2 = opponent,
            currentPlayer = 0
        ).apply {
            state = GamePhase.POWERCARD_DRAWN
        }

        rootService.currentGame = game
        rootService.playerActionService = PlayerActionService(rootService)

        rootService.playerActionService.playPowerCard()

        assertEquals(GamePhase.PLAY_SEVEN_OR_EIGHT, game.state)
        assertTrue(game.log.last().contains("eigene Karte ansehen"))
    }

    @Test
    fun testPlayPowerCard_Ten() {
        val rootService = RootService()
        val player = Player("Alice")
        val opponent = Player("Bob")
        val ten = Card(CardSuit.SPADES, CardValue.TEN, isPowercard = true)

        player.drawnCard = ten

        val game = KabooGame(
            player1 = player,
            player2 = opponent,
            currentPlayer = 0
        ).apply {
            state = GamePhase.POWERCARD_DRAWN
        }

        rootService.currentGame = game
        rootService.playerActionService = PlayerActionService(rootService)

        rootService.playerActionService.playPowerCard()

        assertEquals(GamePhase.PLAY_NINE_OR_TEN, game.state)
        assertTrue(game.log.last().contains("eine Karte von"))
    }


    @Test
    fun testDrawFromPile() {
        val rootService = RootService()
        val game = KabooGame(
            player1 = Player("Alice"),
            player2 = Player("Bob"),
            currentPlayer = 0
        )
        rootService.currentGame = game
        game.state = GamePhase.READYTODRAW

        val topCard = Card(CardSuit.SPADES, CardValue.SEVEN,true)
        game.playStack.push(topCard)

        val testRefreshable = TestRefreshable()
        rootService.playerActionService.addRefreshable(testRefreshable)
        rootService.playerActionService.drawFromPile()
        assertTrue(game.playStack.isEmpty())
        assertEquals(topCard, game.player1.drawnCard)

        assertEquals(GamePhase.DRAW_FROM_PILE, game.state)
        assertTrue(game.log.last().contains("Alice hat die oberste Karte vom Ablagestapel gezogen: ${topCard.value}"))
        assertTrue(testRefreshable.refreshAfterDrawCalled)
    }

    @Test
    fun testPlayPowerCard_InvalidGamePhase_Throws() {
        val rootService = RootService()
        val player = Player("Alice")
        val jack = Card(CardSuit.HEARTS, CardValue.JACK, true)
        player.drawnCard = jack

        val game = KabooGame(player1 = player, player2 = Player("Bob"), currentPlayer = 0)
        game.state = GamePhase.ENDTURN // falsche Phase

        rootService.currentGame = game
        rootService.playerActionService = PlayerActionService(rootService)

        assertThrows<IllegalArgumentException> {
            rootService.playerActionService.playPowerCard()
        }
    }


    @Test
    fun testPlayPowerCard_NonPowerCard_Throws() {
        val rootService = RootService()
        val player = Player("Alice")
        val normalCard = Card(CardSuit.SPADES, CardValue.THREE, isPowercard = false)
        player.drawnCard = normalCard

        val game = KabooGame(player1 = player, player2 = Player("Bob"), currentPlayer = 0)
        game.state = GamePhase.POWERCARD_DRAWN

        rootService.currentGame = game
        rootService.playerActionService = PlayerActionService(rootService)

        assertThrows<IllegalArgumentException> {
            rootService.playerActionService.playPowerCard()
        }
    }


    @Test
    fun testPlayPowerCard_NullDrawnCard_Throws() {
        val rootService = RootService()
        val player = Player("Alice")
        player.drawnCard = null

        val game = KabooGame(player1 = player, player2 = Player("Bob"), currentPlayer = 0)
        game.state = GamePhase.POWERCARD_DRAWN

        rootService.currentGame = game
        rootService.playerActionService = PlayerActionService(rootService)

        assertThrows<IllegalArgumentException> {
            rootService.playerActionService.playPowerCard()
        }
    }


    @Test
    fun testSelectCardOwnCardInDrawFromDeck() {
        val rootService = RootService()
        val player = Player("Alice")
        val opponent = Player("Bob")
        val selectedCard = Card(CardSuit.CLUBS, CardValue.FIVE,false)

        // Spielerhand vorbereiten
        player.hand[0][0] = selectedCard

        val game = KabooGame(
            player1 = player,
            player2 = opponent,
            currentPlayer = 0,

            )
        game.state = GamePhase.DRAW_FROM_DECK
        rootService.currentGame = game

        val testRefreshable = TestRefreshable()
        rootService.playerActionService.addRefreshable(testRefreshable)

        rootService.playerActionService.selectCard(selectedCard)

        assertEquals(listOf(selectedCard), game.selected)

        assertTrue(game.log.last().contains("Alice hat eine Karte ausgewählt"))

        assertTrue(testRefreshable.refreshAfterSelectCalled)
    }

    @Test
    fun testSelectCardInQueenOrJackPhase() {
        val rootService = RootService()
        val player = Player("Alice")
        val opponent = Player("Bob")
        val playerCard = Card(CardSuit.HEARTS, CardValue.QUEEN, true)
        val opponentCard = Card(CardSuit.DIAMONDS, CardValue.KING, false)

        player.hand[0][0] = playerCard
        opponent.hand[0][0] = opponentCard
        val game = KabooGame(
            player1 = player,
            player2 = opponent,
            currentPlayer = 0
        )
        rootService.currentGame = game
        val testRefreshable = TestRefreshable()
        rootService.playerActionService.addRefreshable(testRefreshable)

        game.state = GamePhase.PLAY_QUEEN
        game.selected.clear()
        rootService.playerActionService.selectCard(playerCard)

        assertEquals(listOf(playerCard), game.selected)
        assertTrue(game.log.last().contains("Alice hat eine Karte ausgewählt"))
        assertTrue(testRefreshable.refreshAfterSelectCalled)

        game.selected.clear()
        testRefreshable.refreshAfterSelectCalled = false
        rootService.playerActionService.selectCard(opponentCard)

        assertEquals(listOf(opponentCard), game.selected)
        assertTrue(testRefreshable.refreshAfterSelectCalled)

        game.state = GamePhase.PLAY_JACK
        game.selected.clear()
        testRefreshable.refreshAfterSelectCalled = false
        rootService.playerActionService.selectCard(playerCard)

        assertEquals(listOf(playerCard), game.selected)
        assertTrue(testRefreshable.refreshAfterSelectCalled)

        game.selected.clear()
        testRefreshable.refreshAfterSelectCalled = false
        rootService.playerActionService.selectCard(opponentCard)

        assertEquals(listOf(opponentCard), game.selected)
        assertTrue(testRefreshable.refreshAfterSelectCalled)

        val invalidCard = Card(CardSuit.SPADES, CardValue.ACE, false)
        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.selectCard(invalidCard)
        }
    }

    @Test
    fun testSelectNineOrTenPhase() {
        val rootService = RootService()
        val player = Player("Alice")
        val opponent = Player("Bob")

        val opponentCard = Card(CardSuit.DIAMONDS, CardValue.TEN, false)
        opponent.hand[0][0] = opponentCard
        val game = KabooGame(
            player1 = player,
            player2 = opponent,
            currentPlayer = 0
        )
        rootService.currentGame = game
        val testRefreshable = TestRefreshable()
        rootService.playerActionService.addRefreshable(testRefreshable)

        game.state = GamePhase.PLAY_NINE_OR_TEN

        game.selected.clear()
        testRefreshable.refreshAfterSelectCalled = false
        rootService.playerActionService.selectCard(opponentCard)

        assertEquals(listOf(opponentCard), game.selected)
        assertTrue(testRefreshable.refreshAfterSelectCalled)

        }
    @Test
    fun testSelectSevenOrEightPhase() {
        val rootService = RootService()
        val player = Player("Alice")
        val opponent = Player("Bob")

        val playercard = Card(CardSuit.DIAMONDS, CardValue.TEN, false)
        player.hand[0][0] = playercard
        val game = KabooGame(
            player1 = player,
            player2 = opponent,
            currentPlayer = 0
        )
        rootService.currentGame = game
        val testRefreshable = TestRefreshable()
        rootService.playerActionService.addRefreshable(testRefreshable)

        game.state = GamePhase.PLAY_SEVEN_OR_EIGHT

        game.selected.clear()
        testRefreshable.refreshAfterSelectCalled = false
        rootService.playerActionService.selectCard(playercard)

        assertEquals(listOf(playercard), game.selected)
        assertTrue(testRefreshable.refreshAfterSelectCalled)

    }



    @Test
    fun testSelectCardFailsInWrongGamePhase() {
        val rootService = RootService()
        val player = Player("Alice")
        val opponent = Player("Bob")
        val card = Card(CardSuit.HEARTS, CardValue.TWO,false)
        player.hand[0][0] = card

        val game = KabooGame(
            player1 = player,
            player2 = opponent,
            currentPlayer = 0,

            )
        game.state = GamePhase.INITIALIZED
        rootService.currentGame = game

        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.selectCard(card)
        }
    }


    @Test
    fun testPlayPowerCard_refreshCalled() {
        val rootService = RootService()
        val game = KabooGame(
            player1 = Player("Alice"),
            player2 = Player("Bob"),
            currentPlayer = 0
        )
        rootService.currentGame = game

        val queen = Card(CardSuit.HEARTS, CardValue.QUEEN, true)
        game.player1.drawnCard = queen
        game.state = GamePhase.POWERCARD_DRAWN

        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)

        rootService.playerActionService.playPowerCard()

        assertTrue(testRefreshable.refreshAfterPlayPowerCalled)
    }

    @Test
    fun testPlayPowerCard_cardGoesToPlayStackAfterStateChange() {
        val rootService = RootService()
        val game = KabooGame(
            player1 = Player("Alice"),
            player2 = Player("Bob"),
            currentPlayer = 0
        )
        rootService.currentGame = game

        val powerCard = Card(CardSuit.HEARTS, CardValue.JACK, isPowercard = true)
        game.player1.drawnCard = powerCard
        game.state = GamePhase.POWERCARD_DRAWN

        rootService.playerActionService.playPowerCard()

        assertEquals(GamePhase.PLAY_JACK, game.state)

        assertEquals(powerCard, game.playStack.peek())
    }


    @Test
    fun confirmQueenSwapSwapsCardsCorrectly() {
        val rootService = RootService()
        val gameService = rootService.gameService
        val playerActionService = rootService.playerActionService
        gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        val player = game.player1
        val opponent = game.player2
        game.currentPlayer = 0
        game.state = GamePhase.confirmQueenShow
        val cardFromPlayer = player.hand[0][0]!!
        val cardFromOpponent = opponent.hand[1][1]!!
        val queen = Card(CardSuit.HEARTS, CardValue.QUEEN,true)
        player.drawnCard = queen
        game.selected.add(cardFromPlayer)
        game.selected.add(cardFromOpponent)
        playerActionService.confirmQueenSwap()
        assertEquals(cardFromOpponent, player.hand[0][0])
        assertEquals(cardFromPlayer, opponent.hand[1][1])
        assertEquals(queen, game.playStack.peek())
        assertNull(player.drawnCard)
        assertEquals(GamePhase.READYTODRAW, game.state)
    }


    @Test
    fun testConfirmShowChoiceQueenFailsWithOneCard() {
        val rootService = RootService()
        val game = KabooGame(
            player1 = Player("Alice"),
            player2 = Player("Bob"),
            currentPlayer = 0,

            )
        game.state = GamePhase.PLAY_QUEEN
        rootService.currentGame = game

        val cardOnly = Card(CardSuit.HEARTS, CardValue.SIX,false)
        game.player1.hand[0][0] = cardOnly
        game.selected.add(cardOnly)

        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.confirmChoice()
        }}



        @Test
        fun confirmChoice_PlaySeven_WorksCorrectly() {
            val rootService = RootService()
            rootService.gameService.startNewGame("Alice", "Bob")
            val game = rootService.currentGame!!
            val player = game.player1
            val testRefreshable = TestRefreshable()
            rootService.addRefreshable(testRefreshable)
            game.state = GamePhase.PLAY_SEVEN_OR_EIGHT
            game.currentPlayer = 0
            val selectedCard = player.hand[1][1]
            val seven = Card(CardSuit.HEARTS, CardValue.SEVEN,true)
            player.drawnCard = seven
            game.selected.clear()
            if (selectedCard != null) {
                game.selected.add(selectedCard)
            }

            rootService.playerActionService.confirmChoice()

            assertEquals(GamePhase.READYTODRAW, game.state)
            assertTrue(testRefreshable.refreshAfterConfirmChoiceCalled)
        }






  @Test
    fun testConfirmSwapChoiceFailsWithOneCard() {
        val rootService = RootService()
        val game = KabooGame(
            player1 = Player("Alice"),
            player2 = Player("Bob"),
            currentPlayer = 0,

            )
        game.state = GamePhase.PLAY_JACK
        rootService.currentGame = game

        val cardOnly = Card(CardSuit.CLUBS, CardValue.FOUR,false)
        game.player1.hand[0][0] = cardOnly
        game.selected.add(cardOnly)

        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.confirmChoice()
        }
    }


    @Test
    fun testFindCardPositionInHand() {
        val player = Player("Alice")

        val card1 = Card(CardSuit.HEARTS, CardValue.FIVE,false)
        val card2 = Card(CardSuit.CLUBS, CardValue.NINE,true)

        player.hand[0][1] = card1
        player.hand[1][0] = card2

        val service = PlayerActionService(RootService())

        val pos1 = service.findCardPositionInHand(player, card1)
        val pos2 = service.findCardPositionInHand(player, card2)
        val posNone = service.findCardPositionInHand(player, Card(CardSuit.SPADES, CardValue.KING,false))

        assertEquals(0 to 1, pos1)
        assertEquals(1 to 0, pos2)
        assertNull(posNone)
    }




        @Test
        fun testConfirmChoiceQueenPhase() {
            val rootService = RootService()
            val game = KabooGame(
                player1 = Player("Alice"),
                player2 = Player("Bob"),
                currentPlayer = 0,

                )
            game.state = GamePhase.PLAY_QUEEN
            rootService.currentGame = game

            val playerCard = Card(CardSuit.HEARTS, CardValue.QUEEN, false)
            val opponentCard = Card(CardSuit.DIAMONDS, CardValue.KING, false)

            game.player1.hand[0][0] = playerCard
            game.player2.hand[0][0] = opponentCard
            game.selected = mutableListOf(playerCard, opponentCard)
            game.currentPlayer = 0

         rootService.playerActionService.confirmChoice()

            assertEquals(GamePhase.confirmQueenShow, game.state)

        }

    @Test
    fun testConfirmChoiceInvalidPhase() {
        val rootService = RootService()
        val game = KabooGame(
            player1 = Player("Alice"),
            player2 = Player("Bob"),
            currentPlayer = 0,

            )
        game.state =GamePhase.PLAYERS_ADDED
        rootService.currentGame = game

        // Test & Verify
        assertFailsWith<IllegalStateException> {
 rootService.playerActionService.confirmChoice()
        }
    }

    @Test
    fun testConfirmChoiceQueenWrongSelectionSize() {
        val rootService = RootService()
        val game = KabooGame(
            player1 = Player("Alice"),
            player2 = Player("Bob"),
            currentPlayer = 0,

            )
        game.state =GamePhase.PLAY_QUEEN
        rootService.currentGame = game
        game.selected = mutableListOf(Card(CardSuit.HEARTS, CardValue.QUEEN, false))

        // Test & Verify
        val exception = assertFailsWith<IllegalArgumentException> {
       rootService.playerActionService.confirmChoice()
        }
        assertEquals(" SIZE 2.", exception.message)
    }

    @Test
    fun testConfirmChoiceSevenOrEightWrongOwner() {
        val rootService = RootService()
        val game = KabooGame(
            player1 = Player("Alice"),
            player2 = Player("Bob"),
            currentPlayer = 0,

            )
        game.state =GamePhase.PLAY_SEVEN_OR_EIGHT
        rootService.currentGame = game
        val opponentCard = Card(CardSuit.SPADES, CardValue.SEVEN, false)
        game.player2.hand[0][0] = opponentCard
        game.selected = mutableListOf(opponentCard)
        game.currentPlayer = 0

        // Test & Verify
        val exception = assertFailsWith<IllegalArgumentException> {
        rootService.playerActionService.confirmChoice()
        }
        assertEquals("I AM OWNER .", exception.message)
    }

    @Test
    fun knockEndsGameAfterOneOpponentTurn() {
        val rootService = RootService()
        val game = KabooGame(
            player1 = Player("Alice"),
            player2 = Player("Bob"),
            currentPlayer = 0,

            )
        rootService.currentGame = game
        rootService.playerActionService.knock()
        assertTrue(game.lastRound)

    }


    @Test
    fun testConfirmChoiceNineOrTenWrongOwner() {
        val rootService = RootService()
        val game = KabooGame(
            player1 = Player("Alice"),
            player2 = Player("Bob"),
            currentPlayer = 0,

            )
        game.state =GamePhase.PLAY_NINE_OR_TEN
        rootService.currentGame = game
        val playerCard = Card(CardSuit.SPADES, CardValue.NINE, false)
        game.player1.hand[0][0] = playerCard
        game.selected = mutableListOf(playerCard)
        game.currentPlayer = 0

        val exception = assertFailsWith<IllegalArgumentException> {
   rootService.playerActionService.confirmChoice()
        }
        assertEquals("Die ausgewählte Karte gehört nicht dem Gegner.", exception.message)
    }


    @Test
    fun testKnockDoesNothingIfAlreadyLastRound() {
        val rootService = RootService()
        val player1 = Player("Alice")
        val player2 = Player("Bob")
        val game = KabooGame(player1, player2, currentPlayer = 0).apply {
            lastRound = true
            state = GamePhase.READYTODRAW
        }
        rootService.currentGame = game
        rootService.playerActionService.knock()
        assertEquals(GamePhase.READYTODRAW, game.state) // Zustand bleibt gleich
    }

    @Test
    fun swapCardWithOwnFieldWorksCorrectly() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        val player = game.player1

        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)

        game.state = GamePhase.PUNKTCARD_DRAWN
        game.currentPlayer = 0
        val oldCard = Card(CardSuit.CLUBS, CardValue.FIVE, false)
        player.hand[0][0] = oldCard
        val drawnCard = Card(CardSuit.HEARTS, CardValue.NINE,  true)
        player.drawnCard = drawnCard
        game.selected.clear()
        game.selected.add(oldCard)

        rootService.playerActionService.swapCard()

        assertEquals(drawnCard, player.hand[0][0])
        assertNull(player.drawnCard)
        assertTrue(game.playStack.contains(oldCard))
        assertTrue(game.log.any { it.contains("hat ${drawnCard.value} mit ${oldCard.value} getauscht") })
        assertEquals(GamePhase.READYTODRAW, game.state)
        assertTrue(testRefreshable.refreshAfterSwapCalled)
    }





    @Test
    fun swapCardWithOpponentWorksCorrectly() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        val player = game.player1
        val opponent = game.player2
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)

        val cardFromPlayer = Card(CardSuit.CLUBS, CardValue.FIVE, false)
        player.hand[0][0] = cardFromPlayer

        val cardFromOpponent= Card(CardSuit.CLUBS, CardValue.TWO, false)
        opponent.hand[0][1] = cardFromOpponent

        game.state = GamePhase.PLAY_QUEEN
        game.currentPlayer = 0
        game.selected.clear()
        game.selected.add(cardFromPlayer)
        game.selected.add(cardFromOpponent)

        rootService.playerActionService.swapCard()

        assertEquals(cardFromOpponent, player.hand[0][0])
        assertEquals(cardFromPlayer, opponent.hand[0][1])
        assertEquals(GamePhase.READYTODRAW, game.state)
        assertTrue(testRefreshable.refreshAfterSwapCalled)
    }



    @Test
    fun swapCardWithOpponentWorks() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        val player = game.player1
        val opponent = game.player2
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)

        val cardFromOpponent= Card(CardSuit.CLUBS, CardValue.FIVE, false)
        player.hand[0][0] = cardFromOpponent

        val cardFromPlayer= Card(CardSuit.CLUBS, CardValue.TWO, false)
        opponent.hand[0][1] = cardFromPlayer

        game.state = GamePhase.PLAY_QUEEN
        game.currentPlayer = 0
        game.selected.clear()
        game.selected.add(cardFromPlayer)
        game.selected.add(cardFromOpponent)

        rootService.playerActionService.swapCard()

        assertEquals(cardFromPlayer, player.hand[0][0])
        assertEquals(cardFromOpponent, opponent.hand[0][1])
        assertEquals(GamePhase.READYTODRAW, game.state)
        assertTrue(testRefreshable.refreshAfterSwapCalled)
    }



    @Test
    fun swapCardThrowsExceptionInInvalidState() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.READYTODRAW
        game.currentPlayer = 0

        assertThrows<IllegalStateException> {
            rootService.playerActionService.swapCard()
        }
    }


    @Test
    fun ConfirmQueenShowThrowsIfSizeNot2() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.confirmQueenShow
        val player = game.player1

        game.currentPlayer = 0

        val card  = Card(CardSuit.CLUBS, CardValue.FIVE, false)
        player.hand[0][0] = card

        game.selected.clear()
        game.selected.add(card)

        assertThrows<IllegalArgumentException> {
            rootService.playerActionService.confirmChoice()
        }
    }

    @Test
    fun ConfirmQueenShow_ThrowsIfSamePlayerCards() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        val player = game.player1
        game.state = GamePhase.confirmQueenShow
        game.currentPlayer = 0

        val card1  = Card(CardSuit.CLUBS, CardValue.FIVE, false)
        val card2  = Card(CardSuit.CLUBS, CardValue.TWO, false)
        player.hand[0][0] = card1
        player.hand[1][1] = card2
        game.selected.add(card1)
        game.selected.add(card2)


        assertThrows<IllegalArgumentException> {
            rootService.playerActionService.confirmChoice()
        }
    }

    @Test
    fun confirmChoice_PlayJack_ThrowsIfSizeNot2() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.PLAY_JACK
        val player = game.player1
        game.currentPlayer = 0
        val card  = Card(CardSuit.CLUBS, CardValue.FIVE, false)
        player.hand[0][0] = card

        game.selected.clear()
        game.selected.add(card)

        assertThrows<IllegalArgumentException> {
            rootService.playerActionService.confirmChoice()
        }
    }
    @Test
    fun confirmChoice_PlayJack_ThrowsIfCardsFromSamePlayer() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        val player = game.player1
        game.state = GamePhase.PLAY_JACK
        game.currentPlayer = 0

        val card1  = Card(CardSuit.CLUBS, CardValue.FIVE, false)
        val card2  = Card(CardSuit.CLUBS, CardValue.TWO, false)
        player.hand[0][0] = card1
        player.hand[1][1] = card2
        game.selected.add(card1)
        game.selected.add(card2)


        assertThrows<IllegalArgumentException> {
            rootService.playerActionService.confirmChoice()
        }
    }
    @Test
    fun playJackEffect_AddsLogAndSwapsCard() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.PLAY_JACK
        game.currentPlayer = 0
        val player = game.player1
        val opponent = game.player2

        val playerCard = Card(CardSuit.SPADES, CardValue.ACE, false)
        val opponentCard = Card(CardSuit.HEARTS, CardValue.SIX, false)
        player.hand[0][0] = playerCard
        opponent.hand[0][1] = opponentCard
        game.selected.clear()
        game.selected.add(playerCard)
        game.selected.add(opponentCard)
        rootService.playerActionService.confirmChoice()
        assertEquals(opponentCard, player.hand[0][0])
        assertEquals(playerCard, opponent.hand[0][1])

        assertTrue(game.log.last().contains("Zug beendet"))
    }

    @Test
    fun confirmChoice_PlayNine_WorksCorrectly() {
        val rootService = RootService()
        rootService.gameService.startNewGame("Alice", "Bob")
        val game = rootService.currentGame!!
        game.state = GamePhase.PLAY_NINE_OR_TEN
        game.currentPlayer = 0

        val player = game.player1
        val opponent = game.player2

        val opponentCard = Card(CardSuit.HEARTS, CardValue.SIX, false)
        opponent.hand[0][1] = opponentCard

        game.selected.clear()
        game.selected.add(opponentCard)

        rootService.playerActionService.confirmChoice()

        assertEquals(GamePhase.READYTODRAW, game.state)
    }


}



