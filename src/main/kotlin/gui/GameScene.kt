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
import java.util.*
class GameScene(private val rootService: RootService) : BoardGameScene(1920, 1080), Refreshable {
    private var clickedHandCard: CardView? = null
    private var selectedHandCard: Card? = null
    private val cardImages = CardImageLoader()
    // -------------BUTTONS-------------//
    private val showStartingButton = Button(
        posX = 700,
        posY = 1010,
        width = 150,
        height = 45,
        text = "Show starting Cards"
    ).apply {
        visual = ColorVisual(0, 128, 128)
        onMouseClicked = {
            rootService.gameService.showStartingCards()
        }
    }

    private val hideStartingButton = Button(
        posX = 700,
        posY = 900,
        width = 150,
        height = 45,
        text = "hide starting cards"
    ).apply {
        visual = ColorVisual(0, 128, 128)
        onMouseClicked = {
            rootService.gameService.hideStartingCards()
        }
    }


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

    //knock
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

    //draw from Pile
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

    //discard card
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
    //ConfirmPowerEffect
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
    //-----------Names

    private val player1NameLabel = Label(
        posX = 50,
        posY = 50,
        width = 140,
        height = 60,

        alignment = Alignment.CENTER
    ).apply {
        visual = ColorVisual(30, 144, 255).apply { style.borderRadius = BorderRadius.SMALL }
        font = Font(size = 28, fontWeight = Font.FontWeight.BOLD)
    }

    private val player2NameLabel = Label(
        posX = 1800,
        posY = 1020,
        width = 140,
        height = 60,
        alignment = Alignment.CENTER
    ).apply {
        visual = ColorVisual(30, 144, 255).apply { style.borderRadius = BorderRadius.SMALL }
        font = Font(size = 28, fontWeight = Font.FontWeight.BOLD)
    }

    private val loglabel = Label(
        posX = 200,
        posY = 800,
        width = 1140,
        height = 100,
        alignment = Alignment.CENTER
    ).apply {
        visual = ColorVisual(30, 144, 255).apply { style.borderRadius = BorderRadius.SMALL }
        font = Font(size = 28, fontWeight = Font.FontWeight.BOLD)
    }



    private val player1Grid = CardSquareView(posX = 150.0, posY = 400.0)   // near "Bart"
    private val player2Grid = CardSquareView(posX = 1700.0, posY = 700.0) // near "Zoidberg"



    private val drawPile = LabeledStackView(
        posX = 500,
        posY = 500,
        label = "Draw Stack",
        rotate = false
    ).apply {
        visual = ColorVisual(11, 94, 28)
    }

    private val discardPile = LabeledStackView(
        posX = 1200,
        posY = 500,
        label = "Draw Sck",
        rotate = true
    ).apply {
        visual = ColorVisual(11, 94, 28)
    }


    //_____map
    private val cardMap: BidirectionalMap<Card, CardView> = BidirectionalMap()

    init {
        background = ColorVisual(108, 168, 59)
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
            //currentPlayerLabel,
            discardPile,
            drawPile,
            player1Grid,
            loglabel,
            player2Grid,passButton,
        )
    }


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

    private fun initialLogLabel(label: Label, text: String) {
        label.text = text
        label.font = Font(
            size = 28,
            fontWeight = Font.FontWeight.BOLD
        )
    }


    private fun initialGridView(
        hand: List<Card>,
        handDeckView: CardSquareView,
        cardImageLoader: CardImageLoader,
        cardsToShow: List<Card> = emptyList()
    ) {
        val game = rootService.currentGame ?: return

        val shownCards = when {
            game.showStartingCards -> {
                val starting = when (handDeckView) {
                    player1Grid -> game.player1.startingCards
                    player2Grid -> game.player2.startingCards
                    else -> emptyList()
                }
                starting + cardsToShow
            }
            else -> cardsToShow
        }

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
                    val isCurrentPlayer = (handDeckView == player1Grid && game.currentPlayer == 0) ||
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


    override fun refreshAfterShowStartingCards() {

        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        val player1Cards = game.player1.hand.flatten().filterNotNull()
        val player2Cards = game.player2.hand.flatten().filterNotNull()

        initialGridView(player1Cards, player1Grid, cardImageLoader)
        initialGridView(player2Cards, player2Grid, cardImageLoader)
        initialLogLabel(loglabel, game.log[game.log.size - 1])
    }

    override fun refreshAfterHideStartingCards() {

        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        val player1Cards = game.player1.hand.flatten().filterNotNull()
        val player2Cards = game.player2.hand.flatten().filterNotNull()

        initialGridView(player1Cards, player1Grid, cardImageLoader)
        initialGridView(player2Cards, player2Grid, cardImageLoader)
        initialLogLabel(loglabel, game.log[game.log.size - 1])
    }


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


    override fun refreshAfterHideCards() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        val player1Cards = game.player1.hand.flatten().filterNotNull()
        val player2Cards = game.player2.hand.flatten().filterNotNull()

        initialGridView(player1Cards, player1Grid, cardImageLoader)
        initialGridView(player2Cards, player2Grid, cardImageLoader)
        initialLogLabel(loglabel, game.log[game.log.size - 1])
    }


    override fun refreshAfterDrawDeck() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        initialdrawPile(game.drawPile, drawPile, cardImageLoader)
        initialLogLabel(loglabel, game.log[game.log.size - 1])
    }


    override fun refreshAfterKnock() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()

        // Knock-Button deaktivieren
        knockButton.isDisabled = true

        // Spielerhände aktualisieren
        val player1Cards = game.player1.hand.flatten().filterNotNull()
        val player2Cards = game.player2.hand.flatten().filterNotNull()

        initialGridView(player1Cards, player1Grid, cardImageLoader)
        initialGridView(player2Cards, player2Grid, cardImageLoader)

        // Log-Nachricht mit Hinweis auf letzte Runde
        val knockMsg = game.log.lastOrNull() ?: ""
        val additionalInfo = "Letzte Runde! Jeder Spieler hat noch genau einen Zug."

        initialLogLabel(loglabel, "$knockMsg\n$additionalInfo")
    }


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


    override fun refreshAfterConfirmChoice() {
        val game = rootService.currentGame ?: return
        val cardImageLoader = CardImageLoader()
        val cardsToShow = when (game.state) {
            GamePhase.confirmQueenShow,
            GamePhase.PLAY_SEVEN_OR_EIGHT,
            GamePhase.PLAY_NINE_OR_TEN -> game.selected.toList()

            else -> emptyList()
        }
        val player1Cards = game.player1.hand.flatten().filterNotNull()
        val player2Cards = game.player2.hand.flatten().filterNotNull()

        // Spieleransichten aktualisieren
        initialGridView(player1Cards, player1Grid, cardImageLoader)
        initialGridView(player2Cards, player2Grid, cardImageLoader)

        // Ablagestapel aktualisieren (z.B. falls Powerkarte abgelegt wurde)
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

        // UI-Zustand zurücksetzen
        selectedHandCard = null
        clickedHandCard = null
        // game.selected.clear()

        // Log anzeigen
        initialLogLabel(loglabel, game.log.lastOrNull() ?: "")
    }


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



