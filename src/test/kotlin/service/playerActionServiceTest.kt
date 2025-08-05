package service
import entity.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*
import org.junit.jupiter.api.assertThrows

/**
 * Testklasse für den [PlayerActionService], welche die Spiellogik auf Korrektheit und Robustheit überprüft.
 *
 * Diese Tests decken die folgenden Aspekte ab:
 * - Ziehen von Karten (Power- und Punktkarten)
 * - Ausspielen von Powerkarten (QUEEN, JACK, etc.)
 * - Auswahl und Bestätigung von Karten
 * - Karten-Tauschmechanismen
 * - Knock-Mechanismus und Spielende
 * - Validierung von ungültigen Zuständen und Fehlerfällen
 *
 * Alle Tests nutzen eine [TestRefreshable]-Instanz, um sicherzustellen, dass GUI-Refreshes korrekt aufgerufen werden.
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

    /**
     * Testet das Ziehen einer Powerkarte (Z.B QUEEN).
     * Erwartet:
     * - Die gezogene Karte wird korrekt gespeichert
     * - Die Spielphase wird auf POWERCARD_DRAWN gesetzt
     * - Ein GUI-Refresh wird ausgelöst
     */
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
        assertTrue(refreshable.refreshAfterDrawFromDeckCalled)
    }
    /**
     * Testet das Ziehen einer normalen Punktkarte (keine Powerkarte).
     * Erwartet:
     * - Die Karte wird korrekt gespeichert
     * - Die Spielphase wird auf PUNKTCARD_DRAWN gesetzt
     * - Ein Log-Eintrag wird erstellt
     */
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

        assertTrue(testRefreshable.refreshAfterDrawFromDeckCalled)
    }/**
     * Testet das Ausspielen einer QUEEN-Karte.
     * Erwartet:
     * - Die Karte wird auf den Spielstapel gelegt
     * - Die Spielphase wechselt zu PLAY_QUEEN
     * - Ein Log-Eintrag wird hinzugefügt
     */
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


    /**
     * Testet das Ausspielen der Powerkarte Bube (JACK).
     *
     * Erwartetes Verhalten:
     * - Die gezogene Karte (Bube) wird als Powerkarte erkannt.
     * - Die Spielphase wechselt zu [GamePhase.PLAY_JACK].
     * - Die Karte wird auf den Ablagestapel gelegt.
     * - Ein entsprechender Log-Eintrag mit dem Hinweis auf den Buben wird erzeugt.
     */
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
    /**
     * Testet das Ausspielen der Powerkarte Sieben (SEVEN).
     *
     * Erwartetes Verhalten:
     * - Die gezogene Karte (Sieben) wird als Powerkarte erkannt.
     * - Die Spielphase wechselt zu [GamePhase.PLAY_SEVEN_OR_EIGHT],
     *   wodurch der Spieler eine eigene Karte ansehen darf.
     * - Ein Log-Eintrag weist auf die Möglichkeit hin, eine eigene Karte anzusehen.
     */

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
    /**
     * Testet das Ausspielen der Powerkarte Neun (NINE).
     *
     * Erwartetes Verhalten:
     * - Die gezogene Karte (Neun) wird als Powerkarte erkannt.
     * - Die Spielphase wechselt zu [GamePhase.PLAY_NINE_OR_TEN],
     *   wodurch der Spieler eine Karte des Gegners ansehen darf.
     * - Ein Log-Eintrag bestätigt, dass eine gegnerische Karte angesehen wird.
     */

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
    /**
     * Testet das Ausspielen der Powerkarte Acht (EIGHT).
     *
     * Erwartetes Verhalten:
     * - Die gezogene Karte (Acht) wird als Powerkarte erkannt.
     * - Die Spielphase wechselt zu [GamePhase.PLAY_SEVEN_OR_EIGHT],
     *   wodurch der Spieler eine eigene Karte ansehen darf.
     * - Ein entsprechender Log-Eintrag wird hinzugefügt, der die Aktion dokumentiert.
     */

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
    /**
     * Testet das Ausspielen der Powerkarte Zehn (TEN).
     *
     * Erwartetes Verhalten:
     * - Die gezogene Karte (Zehn) wird als Powerkarte erkannt.
     * - Die Spielphase wird auf [GamePhase.PLAY_NINE_OR_TEN] gesetzt,
     *   sodass der Spieler eine gegnerische Karte ansehen darf.
     * - Ein passender Log-Eintrag dokumentiert die Aktion.
     */

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


    /**
     * Testet das Ziehen der obersten Karte vom Ablagestapel (playStack).
     *
     * Erwartetes Verhalten:
     * - Die oberste Karte wird dem aktuellen Spieler als gezogene Karte zugewiesen.
     * - Der Ablagestapel ist danach leer.
     * - Die Spielphase wechselt zu [GamePhase.DRAW_FROM_PILE].
     * - Ein Log-Eintrag dokumentiert die gezogene Karte.
     * - Das zugehörige Refreshable wird korrekt aufgerufen.
     */

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
        assertTrue(testRefreshable.refreshAfterDrawPileCalled)
    }
    /**
     * Testet das Verhalten von [playPowerCard], wenn die Methode in einer ungültigen Spielphase aufgerufen wird.
     *
     * Erwartetes Verhalten:
     * - Wenn sich das Spiel **nicht** in der Phase [GamePhase.POWERCARD_DRAWN] befindet,
     *   wird eine [IllegalArgumentException] geworfen.
     * - Die Methode darf in anderen Phasen (z.B. [GamePhase.ENDTURN]) nicht aufgerufen werden.
     */

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

    /**
     * Testet das Verhalten von [playPowerCard], wenn eine Karte gespielt wird,
     * die **nicht** als Powerkarte markiert ist.
     *
     * Erwartetes Verhalten:
     * - Eine [IllegalArgumentException] wird geworfen, wenn versucht wird,
     *   eine normale Karte (ohne Spezialeffekt) in der Phase [GamePhase.POWERCARD_DRAWN] auszuspielen.
     */

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
    /**
     * Testet, dass beim Versuch eine Powerkarte zu spielen,
     * aber keine Karte gezogen wurde (`drawnCard == null`),
     * eine [IllegalArgumentException] geworfen wird.
     *
     * Erwartetes Verhalten:
     * - Methode `playPowerCard()` erkennt fehlende gezogene Karte.
     * - Wirft eine Ausnahme zur Verhinderung von ungültigem Spielverlauf.
     */
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
    /**
     * Testet die Auswahl einer eigenen Karte während der Spielphase [GamePhase.DRAW_FROM_DECK].
     *
     * Erwartetes Verhalten:
     * - Die ausgewählte Karte wird korrekt in `game.selected` gespeichert.
     * - Ein Log-Eintrag wird erstellt, der die Auswahl dokumentiert.
     * - Der zugehörige Refreshable wird über `refreshAfterSelect()` benachrichtigt.
     */

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
    /**
     * Testet die Kartenauswahl in den Spielphasen [GamePhase.PLAY_QUEEN] und [GamePhase.PLAY_JACK].
     *
     * Erwartetes Verhalten:
     * - Die Auswahl einer gültigen Karte (von sich selbst oder vom Gegner) funktioniert korrekt.
     * - Die ausgewählte Karte wird korrekt in `game.selected` gespeichert.
     * - Der Log-Eintrag bestätigt die Auswahl.
     * - Der zugehörige Refreshable wird durch `refreshAfterSelect()` benachrichtigt.
     * - Die Auswahl einer ungültigen Karte (nicht auf dem Spielfeld) löst eine [IllegalArgumentException] aus.
     */

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
    /**
     * Testet die Kartenauswahl in der Spielphase [GamePhase.PLAY_NINE_OR_TEN],
     * in der der Spieler eine gegnerische Karte ansehen darf.
     *
     * Erwartetes Verhalten:
     * - Die gegnerische Karte wird korrekt in `game.selected` gespeichert.
     * - Der entsprechende Refreshable wird über `refreshAfterSelect()` benachrichtigt.
     */

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

    /**
     * Testet die Kartenauswahl in der Spielphase [GamePhase.PLAY_SEVEN_OR_EIGHT],
     * in der der Spieler eine eigene Karte aufdecken darf.
     *
     * Erwartetes Verhalten:
     * - Die ausgewählte Karte des Spielers wird korrekt zu `game.selected` hinzugefügt.
     * - Die Methode `refreshAfterSelect()` wird korrekt aufgerufen.
     */

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

    /**
     * Testet, dass die Auswahl einer Karte in einer ungültigen Spielphase (z. B. [GamePhase.INITIALIZED])
     * eine [IllegalArgumentException] auslöst.
     *
     * Erwartetes Verhalten:
     * - Bei einem Aufruf von `selectCard()` außerhalb der erlaubten Spielphasen wird eine Ausnahme geworfen.
     * - Dies schützt die Spiellogik vor inkonsistenter Kartenauswahl.
     */

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

    /**
     * Testet, ob die Methode [PlayerActionService.playPowerCard] die korrekte Refresh-Methode
     * bei registrierten [Refreshable] Instanzen aufruft.
     *
     * Ablauf:
     * - Eine Powerkarte (QUEEN) wird gezogen und das Spiel in die Phase [GamePhase.POWERCARD_DRAWN] versetzt.
     * - Nach dem Aufruf von `playPowerCard()` soll `refreshAfterPlayPowerCalled` beim Refreshable ausgelöst worden sein.
     *
     * Erwartung:
     * - Die GUI oder ein anderes Refresh-System wird korrekt über die gespielte Powerkarte benachrichtigt.
     */

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
    /**
     * Testet, ob eine gezogene Powerkarte korrekt auf den Ablagestapel gelegt wird,
     * nachdem die Spiellogik den Spielzustand entsprechend geändert hat.
     *
     * Ablauf:
     * - Eine Powerkarte (z.B. JACK) wird einem Spieler als gezogene Karte zugewiesen.
     * - Das Spiel befindet sich in der Phase [GamePhase.POWERCARD_DRAWN].
     * - Nach dem Aufruf von [PlayerActionService.playPowerCard] wird erwartet, dass:
     *   - Die Spielphase in [GamePhase.PLAY_JACK] übergeht.
     *   - Die Karte auf dem Ablagestapel ([playStack]) liegt.
     *
     * Erwartung:
     * - Die Powerkarte wird richtig verarbeitet und in den Spielstapel gelegt.
     */

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
    /**
     * Testet die Methode [PlayerActionService.confirmQueenSwap], die den Kartentausch nach dem Ausspielen
     * einer QUEEN-Powerkarte durchführt.
     *
     * Ablauf:
     * - Spieler "Alice" hat eine QUEEN gezogen und wählt eine eigene sowie eine gegnerische Karte aus.
     * - Das Spiel befindet sich in der Phase [GamePhase.confirmQueenShow].
     * - Nach dem Aufruf von [confirmQueenSwap] sollen die beiden Karten getauscht werden.
     *
     * Erwartung:
     * - Die Karten an den jeweiligen Positionen der Spieler sind vertauscht.
     * - Die gezogene QUEEN wird auf den Ablagestapel gelegt.
     * - Die gezogene Karte des Spielers wird entfernt (auf null gesetzt).
     * - Die Spielphase wechselt zu [GamePhase.READYTODRAW].
     */

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
    /**
     * Testet die Methode [PlayerActionService.confirmChoice] im Kontext einer QUEEN-Powerkarte,
     * wenn nur eine Karte ausgewählt wurde.
     *
     * Ablauf:
     * - Das Spiel befindet sich in der Phase [GamePhase.PLAY_QUEEN].
     * - Der Spieler hat nur eine Karte ausgewählt, obwohl für die QUEEN zwei Karten erforderlich sind.
     *
     * Erwartung:
     * - Die Methode [confirmChoice] wirft eine [IllegalArgumentException], da nicht zwei Karten ausgewählt wurden.
     */
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
    /**
     * Testet die Methode [PlayerActionService.confirmChoice] im Fall der Powerkarte SEVEN.
     *
     * Ablauf:
     * - Das Spiel befindet sich in der Phase [GamePhase.PLAY_SEVEN_OR_EIGHT].
     * - Der Spieler hat eine Karte aus der eigenen Hand ausgewählt.
     * - Die gezogene Karte ist eine SEVEN (Powerkarte).
     *
     * Erwartung:
     * - Die gewählte Karte bleibt sichtbar.
     * - Die gezogene Karte (SEVEN) wird auf den Ablagestapel gelegt.
     * - Der Spielzustand wechselt zu [GamePhase.READYTODRAW].
     * - Das Refreshable-Callback [Refreshable.refreshAfterConfirmChoice] wird aufgerufen.
     */
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

    /**
     * Testet, dass [PlayerActionService.confirmChoice] eine Ausnahme wirft,
     * wenn in der Phase [GamePhase.PLAY_JACK] nur eine Karte ausgewählt wurde.
     *
     * Hintergrund:
     * - In der Jack-Phase müssen genau zwei Karten ausgewählt werden, um sie zu tauschen.
     *
     * Erwartung:
     * - Es wird eine [IllegalArgumentException] geworfen, da nur eine Karte ausgewählt wurde.
     */

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
    /**
     * Testet die Methode [PlayerActionService.findCardPositionInHand], die die Position
     * einer bestimmten Karte in der Hand eines Spielers finden soll.
     *
     * Szenario:
     * - Zwei Karten werden manuell in die Hand des Spielers gelegt.
     * - Eine dritte Karte, die nicht in der Hand enthalten ist, wird ebenfalls getestet.
     *
     * Erwartung:
     * - Die Positionen der vorhandenen Karten werden korrekt als Pair<Int, Int> zurückgegeben.
     * - Für eine nicht vorhandene Karte wird `null` zurückgegeben.
     */
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

    /**
     * Testet die Methode [PlayerActionService.confirmChoice] im Spielzustand [GamePhase.PLAY_QUEEN].
     *
     * Szenario:
     * - Zwei Karten (eine eigene und eine gegnerische) werden ausgewählt.
     * - Die Spielphase ist `PLAY_QUEEN`.
     *
     * Erwartung:
     * - Die Spielphase wechselt korrekt zu `confirmQueenShow`.
     * - Dies signalisiert, dass der Spieler nun die Karten tauschen darf.
     */

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
    /**
     * Testet die Methode [PlayerActionService.confirmChoice] in einem ungültigen Spielzustand.
     *
     * Szenario:
     * - Der Spielzustand ist [GamePhase.PLAYERS_ADDED], also ein Zustand, in dem `confirmChoice()` nicht erlaubt ist.
     *
     * Erwartung:
     * - Ein [IllegalStateException] wird ausgelöst, da `confirmChoice()` nur in bestimmten Phasen aufgerufen werden darf.
     */

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
        assertFailsWith<IllegalStateException> {
 rootService.playerActionService.confirmChoice()
        }
    }
    /**
     * Testet die Methode [PlayerActionService.confirmChoice] in der Spielphase [GamePhase.PLAY_QUEEN]
     * mit einer ungültigen Anzahl ausgewählter Karten.
     *
     * Szenario:
     * - Der Spieler befindet sich in der Queen-Phase.
     * - Es wurde nur eine Karte ausgewählt, obwohl zwei Karten (eine eigene und eine vom Gegner) nötig sind.
     *
     * Erwartung:
     * - Eine [IllegalArgumentException] wird ausgelöst mit einer entsprechenden Fehlermeldung über die falsche Anzahl.
     */


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

        val exception = assertFailsWith<IllegalArgumentException> {
       rootService.playerActionService.confirmChoice()
        }
        assertEquals(" SIZE 2.", exception.message)
    }
    /**
     * Testet die Methode [PlayerActionService.confirmChoice] in der Spielphase [GamePhase.PLAY_SEVEN_OR_EIGHT],
     * wenn der Spieler versucht, eine Karte des Gegners aufzudecken.
     *
     * Szenario:
     * - Der aktuelle Spieler befindet sich in der Phase, in der er mit einer 7 oder 8 eine eigene Karte ansehen darf.
     * - Stattdessen wird eine gegnerische Karte ausgewählt.
     *
     * Erwartung:
     * - Eine [IllegalArgumentException] wird mit der Meldung "I AM OWNER ." geworfen, da der Spieler
     *   nur eigene Karten in dieser Phase ansehen darf.
     */

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
    /**
     * Testet die Methode [PlayerActionService.knock], um sicherzustellen, dass das Klopfen
     * den letzten Spielzug korrekt einleitet.
     *
     * Szenario:
     * - Ein aktives Spiel ist vorhanden.
     * - Der aktuelle Spieler ruft die Methode `knock()` auf.
     *
     * Erwartung:
     * - Die Variable `lastRound` wird auf `true` gesetzt, was signalisiert,
     *   dass das Spiel nach dem nächsten Zug des Gegners endet.
     */

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
    /**
     * Testet, dass die Methode [PlayerActionService.confirmChoice] in der Phase [GamePhase.PLAY_NINE_OR_TEN]
     * eine Ausnahme wirft, wenn eine Karte ausgewählt wurde, die nicht dem Gegner gehört.
     *
     * Szenario:
     * - Spielphase ist PLAY_NINE_OR_TEN.
     * - Die ausgewählte Karte gehört dem aktuellen Spieler und nicht dem Gegner.
     *
     * Erwartung:
     * - Ein [IllegalArgumentException] mit der Meldung
     *   "Die ausgewählte Karte gehört nicht dem Gegner." wird geworfen.
     */


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

    /**
     * Testet, dass ein weiterer Knock-Aufruf keine Wirkung zeigt,
     * wenn das Spiel bereits in der letzten Runde ist (`lastRound = true`).
     *
     * Szenario:
     * - Spielzustand ist [GamePhase.READYTODRAW].
     * - `lastRound` wurde bereits zuvor auf `true` gesetzt.
     * - Der Spieler ruft `knock()` auf.
     *
     * Erwartung:
     * - Der Spielzustand bleibt unverändert.
     * - Es erfolgt keine Änderung an der Spiellogik oder dem `lastRound`-Flag.
     */

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
    /**
     * Testet das Tauschen der gezogenen Karte mit einer eigenen Handkarte,
     * wenn der Spieler sich in einem gültigen Zustand befindet ([PUNKTCARD_DRAWN]).
     *
     * Szenario:
     * - Spieler hat eine gezogene Karte.
     * - Eine Karte aus dem eigenen Handfeld wird ausgewählt.
     * - Spielphase erlaubt das Tauschen mit eigener Handkarte.
     *
     * Erwartung:
     * - Die gezogene Karte ersetzt die gewählte Handkarte.
     * - Die alte Handkarte wird auf den Ablagestapel gelegt.
     * - Die gezogene Karte wird aus dem Attribut `drawnCard` entfernt.
     * - Die Spielphase wird zu [GamePhase.READYTODRAW] geändert.
     * - Das Log enthält einen entsprechenden Eintrag.
     * - Das GUI-Refresh (`refreshAfterSwap`) wird korrekt ausgelöst.
     */

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

    /**
     * Testet das Tauschen einer Karte des Spielers mit einer gegnerischen Karte
     * in der Spielphase [GamePhase.PLAY_QUEEN].
     *
     * Szenario:
     * - Der Spieler hat eine gezogene Queen gespielt.
     * - Zwei Karten (eine vom Spieler, eine vom Gegner) wurden ausgewählt.
     * - Die Spielphase erlaubt das Tauschen mit dem Gegner.
     *
     * Erwartung:
     * - Die beiden ausgewählten Karten werden korrekt zwischen Spieler und Gegner getauscht.
     * - Die Spielphase wechselt zu [GamePhase.READYTODRAW].
     * - Das GUI-Refresh (`refreshAfterSwap`) wird ausgelöst.
     */


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


    /**
     * Testet das Tauschen von Karten zwischen Spieler und Gegner in der [GamePhase.PLAY_QUEEN],
     * wobei die Reihenfolge der ausgewählten Karten vertauscht ist (gegnerische Karte zuerst).
     *
     * Szenario:
     * - Die ausgewählten Karten befinden sich jeweils korrekt in den Händen von Spieler und Gegner.
     * - Die Auswahlreihenfolge ist [Gegnerkarte, Spielerkarten], was ebenfalls erlaubt ist.
     *
     * Erwartung:
     * - Die Karten werden korrekt getauscht.
     * - Die Spielphase wechselt zu [GamePhase.READYTODRAW].
     * - Der zugehörige Refresh (`refreshAfterSwap`) wird aufgerufen.
     */

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


    /**
     * Testet, ob `swapCard()` eine [IllegalStateException] wirft, wenn es in einem ungültigen Spielzustand aufgerufen wird.
     *
     * Szenario:
     * - Das Spiel befindet sich in der Phase [GamePhase.READYTODRAW], in der kein Tausch erlaubt ist.
     *
     * Erwartung:
     * - Der Methodenaufruf schlägt mit einer [IllegalStateException] fehl.
     * - Dies stellt sicher, dass `swapCard()` nur in erlaubten Phasen wie
     *   [GamePhase.POWERCARD_DRAWN], [GamePhase.PUNKTCARD_DRAWN], [GamePhase.DRAW_FROM_PILE],
     *   [GamePhase.PLAY_QUEEN] oder [GamePhase.PLAY_JACK] genutzt werden kann.
     */

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

    /**
     * Testet, ob `confirmChoice()` in der Phase [GamePhase.confirmQueenShow] eine
     * [IllegalArgumentException] wirft, wenn nicht genau zwei Karten ausgewählt wurden.
     *
     * Szenario:
     * - Das Spiel befindet sich in der Phase [GamePhase.confirmQueenShow], in der zwei Karten
     *   (eine vom Spieler, eine vom Gegner) ausgewählt sein müssen, um einen Tausch durchzuführen.
     * - Es wurde jedoch nur eine Karte ausgewählt.
     *
     * Erwartung:
     * - Die Methode `confirmChoice()` erkennt die ungültige Auswahl und wirft eine [IllegalArgumentException].
     */

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

        assertThrows<IndexOutOfBoundsException>
       {
            rootService.playerActionService.confirmChoice()
        }
    }
    /**
     * Testet, ob `confirmChoice()` in der Phase [GamePhase.confirmQueenShow] eine
     * [IllegalArgumentException] wirft, wenn beide ausgewählten Karten dem gleichen Spieler gehören.
     *
     * Szenario:
     * - Das Spiel befindet sich in der Phase [GamePhase.confirmQueenShow].
     * - Der Spieler hat zwei eigene Karten ausgewählt anstatt eine eigene und eine gegnerische.
     *
     * Erwartung:
     * - Die Methode `confirmChoice()` erkennt, dass keine Karten von zwei unterschiedlichen Spielern ausgewählt wurden
     *   und wirft eine [IllegalArgumentException].
     */
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
    /**
     * Testet, ob `confirmChoice()` in der Phase [GamePhase.PLAY_JACK] eine
     * [IllegalArgumentException] wirft, wenn weniger als zwei Karten ausgewählt wurden.
     *
     * Szenario:
     * - Das Spiel befindet sich in der Phase [GamePhase.PLAY_JACK].
     * - Der Spieler hat nur eine Karte ausgewählt.
     *
     * Erwartung:
     * - Die Methode `confirmChoice()` erkennt, dass nicht zwei Karten ausgewählt wurden
     *   (eine eigene und eine gegnerische) und wirft eine [IllegalArgumentException].
     */

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

    /**
     * Testet, ob `confirmChoice()` in der Phase [GamePhase.PLAY_JACK] eine
     * [IllegalArgumentException] wirft, wenn beide ausgewählten Karten vom selben Spieler stammen.
     *
     * Szenario:
     * - Das Spiel befindet sich in der Phase [GamePhase.PLAY_JACK].
     * - Der Spieler wählt zwei Karten aus seiner eigenen Hand aus.
     *
     * Erwartung:
     * - Die Methode `confirmChoice()` erkennt, dass keine Karte vom gegnerischen Spieler stammt,
     *   und wirft eine [IllegalArgumentException], da ein Tausch mit einer gegnerischen Karte
     *   erforderlich ist.
     */

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

    /**
     * Testet die Ausführung des Buben-Effekts (`playJackEffect`) im Rahmen der Methode `confirmChoice()`.
     *
     * Szenario:
     * - Das Spiel befindet sich in der Phase [GamePhase.PLAY_JACK].
     * - Zwei Karten – eine vom Spieler, eine vom Gegner – sind ausgewählt.
     *
     * Erwartung:
     * - Die Karten werden korrekt zwischen den Spielern getauscht.
     * - Ein entsprechender Log-Eintrag über den Tausch wird hinzugefügt.
     * - Der Spielzustand wechselt am Ende zu [GamePhase.READYTODRAW] durch `endTurn()`.
     */

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

    /**
     * Testet das korrekte Verhalten von `confirmChoice()` in der Phase [GamePhase.PLAY_NINE_OR_TEN].
     *
     * Szenario:
     * - Der aktuelle Spieler hat eine gegnerische Karte ausgewählt (z. B. nach dem Ziehen einer Neun oder Zehn).
     *
     * Erwartung:
     * - Die Methode `showCards()` wird korrekt aufgerufen (indirekt).
     * - Der Spielzustand wechselt anschließend zu [GamePhase.READYTODRAW].
     */

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




