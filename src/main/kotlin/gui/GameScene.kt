package gui
import service.Refreshable
import entity.*
import service.RootService
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color
import java.util.*

/**
 * Main game scene for the Kaboo card game.
 *
 * This scene displays the full playing field, player hands,
 * draw/discard piles, player names, and action buttons.
 * It also reacts to game events via [Refreshable] to update the UI
 * according to the current game state.
 *
 * @property rootService Reference to the central [RootService] handling all game logic and state.
 */
class GameScene(private val rootService: RootService) : BoardGameScene(1920, 1080), Refreshable {
    /** Last clicked card's [CardView], if any. */
    private var clickedHandCard: CardView? = null
    /** Last selected card's [Card] data, if any. */
    private var selectedHandCard: Card? = null
    /** Loader for all card images used in the game. */
    private val cardImages = CardImageLoader()
    // -------------BUTTONS-------------
    /** Button to reveal both players' starting cards. */
    private val showStartingButton = Button(
        posX = 700,
        posY = 1010,
        width = 150,
        height = 45,
        text = "Show starting Cards"
    ).apply {
        visual = ColorVisual(245, 245, 220)

        onMouseClicked = {
            rootService.gameService.showStartingCards()
        }
    }
    /** Button to hide both players' starting cards again. */
    private val hideStartingButton = Button(
        posX = 700,
        posY = 900,
        width = 150,
        height = 45,
        text = "hide starting cards"
    ).apply {
        visual = ColorVisual(245, 245, 220)

        onMouseClicked = {
            rootService.gameService.hideStartingCards()
        }
    }
    /** Button to draw a card from the draw pile. */

    private val drawFromDeckButton = Button(
        posX = 255,
        posY = 1010,
        width = 120,
        height = 45,
        text = "draw from deck",

        ).apply {
        visual = ColorVisual(0, 128, 0)
        onMouseClicked = {
            rootService.playerActionService.drawFromDeck()
        }
    }

    /** Button to knock (signal the last round). */
    private val knockButton = Button(
        posX = 5,
        posY = 1010,
        width = 120,
        height = 45,
        text = "knock",
    ).apply {
        visual = ColorVisual(0, 128, 0) // Dunkelgrün

        onMouseClicked = {
            rootService.playerActionService.knock()
        }
    }
    /** Button to swap a selected card. */

    private val swapButton = Button(
        posX = 130,
        posY = 1010,
        width = 120,
        height = 45,
        text = "Swap"
    ).apply {
        visual = ColorVisual(0, 128, 0)
        onMouseClicked = {
            if (selectedHandCard != null) {
                rootService.playerActionService.swapCard()
                selectedHandCard = null
                clickedHandCard = null

            } else {
                println("No card selected for swap")
            }
        }
    }

    /** Button to draw a card from the discard pile. */
    private val drawFromPileButton = Button(
        posX = 380,
        posY = 1010,
        width = 120,
        height = 45,
        text = "draw from pile",

        ).apply {
        visual = ColorVisual(0, 128, 0)
        onMouseClicked = { rootService.playerActionService.drawFromPile() }
    }

    /** Button to discard the currently drawn card. */
    private val discardButton = Button(
        posX = 505,
        posY = 1010,
        width = 120,
        height = 45,
        text = "discard card",

        ).apply {
        visual = ColorVisual(0, 128, 0)
        onMouseClicked = {
            rootService.gameService.discardCard()

        }
    }

    /** Button to skip swap in case of Queen if you just want to see the cards and decided not to swap */
    private val passButton = Button(
        posX = 300,
        posY = 700,
        width = 120,
        height = 45,
        text = "Skip Swap"

    ).apply {
        visual = ColorVisual(0, 128, 0)
        onMouseClicked = {
            rootService.playerActionService.cancelPowerEffect()
        }


    }
    /** Button to confirm using a power card effect , in case of queenEffect you click on it two times, the first time to see the cards than in case you decided to swap the cards */
    private val confirmPowerButton = Button(
        posX = 300,
        posY = 750,
        width = 120,
        height = 45,
        text = "Confirm"

    ).apply {
        visual = ColorVisual(0, 128, 0)
        onMouseClicked = {
            rootService.playerActionService.confirmChoice()
        }
    }
    //-----------Labels
    /** Displays player 1's name. */
    private val player1NameLabel = Label(
        posX = 50,
        posY = 50,
        width = 140,
        height = 60,

        alignment = Alignment.CENTER
    ).apply {
        // Gold background
        visual = ColorVisual(255, 215, 0).apply {
            style.borderRadius = BorderRadius.SMALL
        }
        // Black text for contrast
        font = Font(size = 28, fontWeight = Font.FontWeight.BOLD, color = tools.aqua.bgw.core.Color(0, 0, 0))
    }
    /** Displays player 2's name. */
    private val player2NameLabel = Label(
        posX = 1770,
        posY = 1000,
        width = 140,
        height = 60,
        alignment = Alignment.CENTER
    ).apply {
        // Gold background
        visual = ColorVisual(255, 215, 0).apply {
            style.borderRadius = BorderRadius.SMALL
        }
        // Black text for contrast
        font = Font(size = 28, fontWeight = Font.FontWeight.BOLD, color = tools.aqua.bgw.core.Color(0, 0, 0))
    }

    /** Displays the game log. */
    private val loglabel = Label(
        posX = 200,
        posY = 800,
        width = 1100,
        height = 50,
        alignment = Alignment.CENTER
    ).apply {
        visual = ColorVisual(255, 215, 0).apply { style.borderRadius = BorderRadius.SMALL }
        font = Font(size = 28, fontWeight = Font.FontWeight.BOLD)
    }

    // --------- CARD AREAS -----------
    /** Player 1's 2×2 hand grid. */
    private val player1Grid = CardSquareView(posX = 150.0, posY = 400.0)   // near "Bart"
    /** Player 2's 2×2 hand grid. */
    private val player2Grid = CardSquareView(posX = 1700.0, posY = 700.0) // near "Zoidberg"

    /** Draw pile view. */
    private val drawPile = LabeledStackView(
        posX = 500,
        posY = 500,
        label = "Draw Stack",
        rotate = false
    ).apply {
        visual = ColorVisual(11, 94, 28)
    }
    /** Discard pile view. */
    private val discardPile = LabeledStackView(
        posX = 1200,
        posY = 500,
        label = "Draw Sck",
        rotate = true
    ).apply {
        visual = ColorVisual(11, 94, 28)
    }

    /** Maps each [Card] to its corresponding [CardView] in the scene. */
    private val cardMap: BidirectionalMap<Card, CardView> = BidirectionalMap()

    init {
        background = ImageVisual("tapis.PNG")
        addComponents(
            player1NameLabel,
            player2NameLabel,
            knockButton,
            swapButton,
            drawFromDeckButton,
            drawFromPileButton,
            discardButton,
            confirmPowerButton,
            showStartingButton,
            hideStartingButton,
            discardPile,
            drawPile,
            player1Grid,
            loglabel,
            player2Grid,passButton,
        )
    }

    /**
     * Initializes the given [LabeledStackView] with cards from a stack,
     * showing their back sides (for the draw pile).
     */
    private fun initialdrawPile(
        stack: Stack<Card>, stackView: LabeledStackView, cardImageLoader: CardImageLoader
    ) {
        stackView.clear()

        stack.asReversed().forEach { card ->
            val cardView = CardView(
                height = 200,
                width = 130,
                front = cardImageLoader.frontImageFor(card.suit, card.value),
                back = cardImageLoader.backImage
            ).apply { showBack() }
            stackView.add(cardView)
            cardMap.add(card to cardView)
        }
    }

    /**
     * Initializes the given [LabeledStackView] with cards from a stack,
     * showing their front sides (for the discard pile).
     */
    private fun initialdiscardPile(
        stack: Stack<Card>,
        stackView: LabeledStackView,
        cardImageLoader: CardImageLoader
    ) {
        stackView.clear()
        stack.asReversed().forEach { card ->
            val cardView = CardView(
                height = 200,
                width = 130,
                front = cardImageLoader.frontImageFor(card.suit, card.value),
                back = cardImageLoader.backImage
            ).apply {
                showFront()
            }
            stackView.add(cardView)
            cardMap.add(card to cardView)
        }
    }
    /** Sets up a player's name label with bold text. */
    private fun initialPlayerNameLabel(label: Label, text: String) {
        label.text = text
        label.font = Font(
            size = 28,
            fontWeight = Font.FontWeight.BOLD
        )
    }

    private fun initialCurrentPlayerNameLabel(label: Label, text: String) {
        label.text = text
        label.font = Font(
            size = 28,
            fontWeight = Font.FontWeight.BOLD
        )
    }
    /** Sets up the game log label. */
    private fun initialLogLabel(label: Label, text: String) {
        label.text = text
        label.font = Font(
            size = 28,
            fontWeight = Font.FontWeight.BOLD
        )
    }

    /**
     * Displays a player's hand in their [CardSquareView].
     * @param hand List of [Card] objects in the player's hand.
     * @param handDeckView The grid view to display the cards in.
     * @param cardImageLoader Loader for card images.
     * @param cardsToShow Optional cards to be revealed regardless of game state.
     */

    private fun initialGridView(
        hand: List<Card>,
        handDeckView: CardSquareView,
        cardImageLoader: CardImageLoader,
        cardsToShow: List<Card> = emptyList()
    ) {
        val game = rootService.currentGame ?: return

        // Nur das verwenden, was der Aufrufer explizit anzeigen will
        val shownCards = cardsToShow

        val cardViews = hand.map { card ->
            CardView(
                height = 200,
                width = 130,
                front = cardImageLoader.frontImageFor(card.suit, card.value),
                back = cardImageLoader.backImage
            ).apply {
                if (card in shownCards || card.isRevealed) {
                    showFront()
                } else {
                    showBack()
                }
                onMouseClicked = {
                    val isCurrentPlayer =
                        (handDeckView == player1Grid && game.currentPlayer == 0) ||
                                (handDeckView == player2Grid && game.currentPlayer == 1)

                    if (isCurrentPlayer || game.state in listOf(
                            GamePhase.PLAY_QUEEN,
                            GamePhase.PLAY_JACK,
                            GamePhase.PLAY_NINE_OR_TEN,
                            GamePhase.PLAY_SEVEN_OR_EIGHT,
                            GamePhase.POWERCARD_DRAWN,
                            GamePhase.PUNKTCARD_DRAWN
                        )
                    ) {
                        selectCard(card, this)
                    }
                }
            }.also { cardMap.add(card to it) }
        }

        handDeckView.displayCards(cardViews)
    }



    /**
     * Handles selecting a card in the UI and forwarding
     * the selection to the [RootService].
     */
    private fun selectCard(card: Card, cardView: CardView) {
        val game = rootService.currentGame ?: return

        try {
            selectedHandCard = card
            clickedHandCard = cardView

            rootService.playerActionService.selectCard(card)
        } catch (e: Exception) {
            println("Card selection failed: ${e.message}")
            selectedHandCard = null
            clickedHandCard = null
        }
    }
    // Refreshable Overrides ------


    /**
     * Called after a new game starts.
     *
     * Resets the draw/discard piles, clears the card map,
     * updates player names, fills both player grids with their starting hands,
     * and shows the initial log message.
     */

    override fun refreshAfterStartNewGame() {
        val game = rootService.currentGame ?: return
        cardMap.clear()
        val cardImageLoader = CardImageLoader()

        initialdrawPile(game.drawPile, drawPile, cardImageLoader)
        initialdiscardPile(game.playStack, discardPile, cardImageLoader)

        val player1Cards = game.player1.hand.flatten().filterNotNull()
        val player2Cards = game.player2.hand.flatten().filterNotNull()

        player1NameLabel.text = game.player1.name
        player2NameLabel.text = game.player2.name

        initialPlayerNameLabel(player1NameLabel, game.player1.name)
        initialPlayerNameLabel(player2NameLabel, game.player2.name)

        initialGridView(player1Cards, player1Grid, cardImageLoader)
        initialGridView(player2Cards, player2Grid, cardImageLoader)

        initialLogLabel(loglabel, game.log[game.log.size - 1])
    }

    /**
     * Called after the "Show starting cards" action is triggered.
     *
     * Updates both player grids so that all starting cards are visible,
     * while keeping the rest of the state unchanged.
     */


    override fun refreshAfterShowStartingCards() {
        val game = rootService.currentGame ?: return
        val loader = CardImageLoader()

        val p1Cards = game.player1.hand.flatten().filterNotNull()
        val p2Cards = game.player2.hand.flatten().filterNotNull()

        if (game.currentPlayer == 0) {
            // show ONLY player 1's starting cards
            initialGridView(p1Cards, player1Grid, loader, cardsToShow = game.player1.startingCards)
            // opponent stays hidden (normal visibility)
            initialGridView(p2Cards, player2Grid, loader, cardsToShow = emptyList())
        } else {
            // show ONLY player 2's starting cards
            initialGridView(p2Cards, player2Grid, loader, cardsToShow = game.player2.startingCards)
            initialGridView(p1Cards, player1Grid, loader, cardsToShow = emptyList())
        }

        initialLogLabel(loglabel, game.log.last())
    }
    /**
     * Called after the "Hide starting cards" action is triggered.
     *
     * Updates both player grids to hide the starting cards again,
     * only showing revealed cards or those marked as visible.
     */
    override fun refreshAfterHideStartingCards() {
        val game = rootService.currentGame ?: return
        val loader = CardImageLoader()

        val p1Cards = game.player1.hand.flatten().filterNotNull()
        val p2Cards = game.player2.hand.flatten().filterNotNull()

        // hide again: pass no extra cards to show
        initialGridView(p1Cards, player1Grid, loader, cardsToShow = emptyList())
        initialGridView(p2Cards, player2Grid, loader, cardsToShow = emptyList())

        initialLogLabel(loglabel, game.log.last())
    }

    /**
     * Called after the "Show cards" effect from a power card is triggered.
     *
     * Reveals the selected cards for the relevant player
     * without altering the other player's hand visibility.
     *
     * @param card1 The first revealed card.
     * @param card2 An optional second revealed card.
     */

    override fun refreshAfterShowCards(card1: Card, card2: Card?) {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        val cardsToShow = if (game.currentPlayer == 0) {
            game.player1.hand.flatten().filterNotNull()
        } else {
            game.player2.hand.flatten().filterNotNull()
        }

        if (game.currentPlayer == 1) {
            initialGridView(cardsToShow, player1Grid, cardImageLoader)
        } else {
            initialGridView(cardsToShow, player2Grid, cardImageLoader)
        }
    }


    /**
     * Called after hiding cards that were previously revealed
     * (e.g., after a show effect ends).
     *
     * Resets both player grids to their normal visibility rules.
     */
    override fun refreshAfterHideCards() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        val player1Cards = game.player1.hand.flatten().filterNotNull()
        val player2Cards = game.player2.hand.flatten().filterNotNull()

        initialGridView(player1Cards, player1Grid, cardImageLoader)
        initialGridView(player2Cards, player2Grid, cardImageLoader)
        initialLogLabel(loglabel, game.log[game.log.size - 1])
    }

    /**
     * Called after a player draws from the deck.
     *
     * Updates the draw pile display and appends the latest log entry.
     */

    override fun refreshAfterDrawDeck() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        initialdrawPile(game.drawPile, drawPile, cardImageLoader)
        initialLogLabel(loglabel, game.log[game.log.size - 1])
    }

    /**
     * Called after a player knocks.
     *
     * Disables the knock button, updates both player grids,
     * and updates the log with an additional message indicating the last round.
     */
    override fun refreshAfterKnock() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        knockButton.isDisabled = true

        val player1Cards = game.player1.hand.flatten().filterNotNull()
        val player2Cards = game.player2.hand.flatten().filterNotNull()

        initialGridView(player1Cards, player1Grid, cardImageLoader)
        initialGridView(player2Cards, player2Grid, cardImageLoader)

        val knockMsg = game.log.lastOrNull() ?: ""
        val additionalInfo = "Letzte Runde! Jeder Spieler hat noch genau einen Zug."

        initialLogLabel(loglabel, "$knockMsg\n$additionalInfo")
    }

    /**
     * Called after a swap action occurs.
     *
     * Depending on the game state, updates either both player grids (for Queen/Jack swaps)
     * or just the current player's grid. Also refreshes the discard pile if needed.
     */
    override fun refreshAfterSwap() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()
        val topCard: Card?
        val player1HandCards = game.player1.hand.flatten().filterNotNull()
        val player2HandCards = game.player2.hand.flatten().filterNotNull()
        if (game.state == GamePhase.PLAY_QUEEN || game.state == GamePhase.PLAY_JACK) {
            initialGridView(player1HandCards, player1Grid, cardImageLoader)
            initialGridView(player2HandCards, player2Grid, cardImageLoader)
            initialLogLabel(loglabel, game.log[game.log.size - 1])
        } else if (
            game.state == GamePhase.POWERCARD_DRAWN ||
            game.state == GamePhase.PUNKTCARD_DRAWN ||
            game.state == GamePhase.DRAW_FROM_PILE
        ) {
            if (game.currentPlayer == 0) {
                initialGridView(player1HandCards, player1Grid, cardImageLoader)
            } else {
                initialGridView(player2HandCards, player2Grid, cardImageLoader)
            }

            val topDiscard = game.playStack.peek()
            if (topDiscard != null) {
                val cardView = CardView(
                    width = 130.0,
                    height = 200.0,
                    front = cardImageLoader.frontImageFor(topDiscard.suit, topDiscard.value),
                    back = cardImageLoader.backImage
                ).apply {
                    showFront()
                }
                discardPile.clear()
                discardPile.add(cardView)
                cardMap.add(topDiscard to cardView)
            }

            initialLogLabel(loglabel, game.log.lastOrNull() ?: "")
        }
    }
    /**
     * Called after a player draws from the discard pile.
     *
     * Updates the discard pile display to reflect the remaining cards
     * and appends the latest log message.
     */

    override fun refreshAfterDrawPile() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        initialdiscardPile(game.playStack, discardPile, cardImageLoader)

        val topCard = game.playStack.lastOrNull()
        if (topCard != null) {
            val cardView = CardView(
                width = 120.0,
                height = 180.0,
                front = cardImageLoader.frontImageFor(topCard.suit, topCard.value),
                back = cardImageLoader.backImage
            ).apply {
                showFront()
            }
            cardMap.add(topCard to cardView)
        }
        initialLogLabel(loglabel, game.log[game.log.size - 1])
    }

    /**
     * Called after a player discards a card.
     *
     * Adds the discarded card to the discard pile and updates the log.
     */
    override fun refreshAfterDiscard() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()
        val topCard: Card?

        if (game.currentPlayer == 0) {
            topCard = game.player1.drawnCard
        } else {
            topCard = game.player2.drawnCard
        }

        if (topCard != null) {
            val cardView = CardView(
                width = 120.0,
                height = 180.0,
                front = cardImageLoader.frontImageFor(topCard.suit, topCard.value),
                back = cardImageLoader.backImage
            ).apply {
                showFront()
            }
            discardPile.add(cardView)
            cardMap.add(topCard to cardView)
        }
        initialLogLabel(loglabel, game.log[game.log.size - 1])
    }
    /**
     * Called after the "Confirm choice" button is pressed
     * during a power card effect.
     *
     * Updates the affected player grids, refreshes the discard pile
     * (in case the played card was discarded), and clears any selected cards.
     */

    override fun refreshAfterConfirmChoice() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()
        val cardsToShow = when (game.state) {
            GamePhase.CONFIRMQUEENSHOW,
            GamePhase.PLAY_SEVEN_OR_EIGHT,
            GamePhase.PLAY_NINE_OR_TEN -> game.selected.toList()

            else -> emptyList()
        }
        val player1Cards = game.player1.hand.flatten().filterNotNull()
        val player2Cards = game.player2.hand.flatten().filterNotNull()
        initialGridView(player1Cards, player1Grid, cardImageLoader)
        initialGridView(player2Cards, player2Grid, cardImageLoader)
        discardPile.clear()
        val topCard = game.playStack.peek()
        if (topCard != null) {
            val topCardView = CardView(
                width = 120.0,
                height = 180.0,
                front = cardImageLoader.frontImageFor(topCard.suit, topCard.value),
                back = cardImageLoader.backImage
            ).apply { showFront() }

            discardPile.add(topCardView)
            cardMap.add(topCard to topCardView)
        }

        selectedHandCard = null
        clickedHandCard = null
        initialLogLabel(loglabel, game.log.lastOrNull() ?: "")
    }

    override fun refreshAfterSelect() {
        val game = rootService.currentGame ?: return
        val latestLogEntry = game.log.lastOrNull() ?: ""
        initialLogLabel(loglabel, game.log[game.log.size - 1])
    }

    /**
     * Called after a player's turn ends.
     *
     * Refreshes both player grids, hides all revealed cards,
     * and clears any current selections.
     */

    override fun refreshAfterTurnEnd() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        val player1Cards = game.player1.hand.flatten().filterNotNull()
        val player2Cards = game.player2.hand.flatten().filterNotNull()

        initialGridView(player1Cards, player1Grid, cardImageLoader)
        initialGridView(player2Cards, player2Grid, cardImageLoader)
        game.player1.hand.flatten().forEach { it?.isRevealed = false }
        game.player2.hand.flatten().forEach { it?.isRevealed = false }

        selectedHandCard = null
        clickedHandCard = null
    }

    /**
     * Called after a new turn starts.
     *
     * Refreshes both player grids and clears any selected card state.
     */
    override fun refreshAfterStartTurn() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        val player1Cards = game.player1.hand.flatten().filterNotNull()
        val player2Cards = game.player2.hand.flatten().filterNotNull()

        initialGridView(player1Cards, player1Grid, cardImageLoader)
        initialGridView(player2Cards, player2Grid, cardImageLoader)

        selectedHandCard = null
        clickedHandCard = null
    }
}



