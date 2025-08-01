package service
import entity.*
/**
 * Service layer class that provides the logic for the possible actions a player can take in the game
 *
 * @param rootService The [RootService] instance to access the other service methods and entity layer
 */
class PlayerActionService (private val rootService: RootService) : AbstractRefreshingService() {
    fun currentPlayer(): Player {
        val game = checkNotNull(rootService.currentGame) { "Kein Spiel aktiv." }
        return if (game.currentPlayer == 0) game.player1 else game.player2
    }

    fun playPowerCard() {
        val game = rootService.currentGame
        checkNotNull(game)
        require(game.state == GamePhase.POWERCARD_DRAWN) {
            "Powerkarten dürfen nur direkt nach dem Ziehen gespielt werden)."
        }
        require(game.currentPlayer == 1 || game.currentPlayer == 0) { "There is no active player: $game expected 1 or 0" }

        val player = if (game.currentPlayer == 0) game.player1 else game.player2
        val gegner = if (game.currentPlayer == 0) game.player2 else game.player1

        var card = player.drawnCard
        require(card?.isPowerCard() == true) {
            "Die übergebene Karte ist keine Powerkarte."
        }

        if (card != null) {
            when (card.value) {
                CardValue.JACK -> {
                    game.state = GamePhase.PLAY_JACK
                    game.log.add("${player.name} soll mit Bube blind Karte mit ${gegner.name}'s Karte tauschen")

                }

                CardValue.QUEEN -> {
                    game.state = GamePhase.PLAY_QUEEN
                    game.log.add("${player.name} sieht mit QUEEN  Karte von ${gegner.name} und darf austauschen")

                }
                CardValue.SEVEN, CardValue.EIGHT -> {
                    game.state = GamePhase.PLAY_SEVEN_OR_EIGHT
                    game.log.add("${player.name} darf mit ${card.value} eigene Karte ansehen.")
                }

                CardValue.NINE, CardValue.TEN -> {
                    game.state = GamePhase.PLAY_NINE_OR_TEN
                    game.log.add(
                        "${player.name} darf mit ${card.value} eine Karte von " +
                                "(${gegner.name}) ansehen."
                    )
                }
                else -> return
            }
        }
        if (card != null) {
            game.playStack.push(card) }
        onAllRefreshables { refreshAfterPlayPower() } }

        fun swapCard() {
        val game = rootService.currentGame
        checkNotNull(game)
        val player = if (game.currentPlayer == 0) game.player1 else game.player2
        val gegner = if (game.currentPlayer == 0) game.player2 else game.player1
            when (game.state) {
            GamePhase.POWERCARD_DRAWN,
            GamePhase.PUNKTCARD_DRAWN,
            GamePhase.DRAW_FROM_PILE -> {
                swapWithOwnField(game, player)
            }

            GamePhase.PLAY_QUEEN, GamePhase.PLAY_JACK -> {
                swapCardsInternal(game, player, gegner)
            }
                else -> throw IllegalStateException("In Phase ${game.state} darf nicht getauscht werden.")
        }
    }

      private fun swapCardsInternal(game: KabooGame, player: Player, gegner: Player) {
        require(game.state == GamePhase.PLAY_QUEEN || game.state == GamePhase.PLAY_JACK) {
            "Karten dürfen nur mit Dame oder Bube getauscht werden."
        }
        var posCurrentPlayer: Pair<Int, Int>? = null
        var posOppPlayer: Pair<Int, Int>? = null


        if (player.hand.flatten().contains(game.selected[0])) {

            posCurrentPlayer = findCardPositionInHand(player, game.selected[0] )
            posOppPlayer = findCardPositionInHand(gegner, game.selected[1] )
        }
        else  {
            posCurrentPlayer = findCardPositionInHand(player, game.selected[1] )
            posOppPlayer = findCardPositionInHand(gegner, game.selected[0] )

        }

        requireNotNull(posCurrentPlayer) { "Karte nicht im Handfeld gefunden." }
        requireNotNull(posOppPlayer) { "Karte nicht im Handfeld gefunden." }


        val (row1, col1) = posCurrentPlayer
        val (row2, col2) = posOppPlayer

        val tmp = player.hand[row1][col1]
        player.hand[row1][col1] = gegner.hand[row2][col2]
        gegner.hand[row2][col2] = tmp

        onAllRefreshables { refreshAfterSwap() }
        game.state = GamePhase.ENDTURN
        rootService.gameService.endTurn()
    }

    private fun swapWithOwnField(game: KabooGame, player: Player) {
        require(
            game.state == GamePhase.POWERCARD_DRAWN ||
                    game.state == GamePhase.PUNKTCARD_DRAWN ||
                    game.state == GamePhase.DRAW_FROM_PILE
        )
        {
            "Tauschen ist nur direkt nach dem Ziehen einer Karte erlaubt."
        }
        val drawncard = player.drawnCard

        val pos = findCardPositionInHand(player, game.selected[0])
        requireNotNull(pos) { "Karte nicht im Handfeld gefunden." }
        val (row, col) = pos

        player.hand[row][col] = drawncard
        game.playStack.push(game.selected[0])
        player.drawnCard = null
        game.log.add("${player.name} hat ${drawncard?.value} mit ${game.selected[0].value} getauscht .")

        onAllRefreshables { refreshAfterSwap() }
        game.state = GamePhase.ENDTURN
        rootService.gameService.endTurn()
    }

       fun drawFromDeck() {
        val game = rootService.currentGame
        checkNotNull(game)

        if (game.drawPile.isEmpty()){
            rootService.gameService.endTurn()
        }

        require(game.state == GamePhase.READYTODRAW) {
            "you must draw"
        }

        val card = game.drawPile.pop()

        currentPlayer().drawnCard = card

        game.state = GamePhase. DRAW_FROM_DECK
        if (card.isPowerCard()) {
            card.isPowercard =true
            game.log.add("${currentPlayer().name} hat eine Powerkarte (${card.value}) vom Nachziehstapel gezogen.")
            game.state = GamePhase.POWERCARD_DRAWN


        } else {
            game.log.add("${currentPlayer().name} hat eine Punktekarte (${card.value}) vom Nachziehstapel gezogen.")
            game.state = GamePhase.PUNKTCARD_DRAWN
        }

        if (game.drawPile.isEmpty()){
            game.lastRound = true
        }

        onAllRefreshables { refreshAfterDraw() }
    }


    fun drawFromPile() {
        val game = rootService.currentGame
        checkNotNull(game)
        require(game.playStack.isNotEmpty()) {
            "Der Ablagestapel ist leer – es kann keine Karte gezogen werden."
        }
        require(game.state==GamePhase. READYTODRAW) {
            "you must draw"
        }

        val topCard = game.playStack.pop()
        currentPlayer().drawnCard = topCard
        game.state = GamePhase.DRAW_FROM_PILE
        game.log.add("${currentPlayer().name} hat die oberste Karte vom Ablagestapel gezogen: ${topCard.value}.")
        onAllRefreshables { refreshAfterDraw() }
    }

    fun selectCard(card: Card) {
        val game = rootService.currentGame
        checkNotNull(game) { "Kein aktives Spiel vorhanden." }

        val player = if (game.currentPlayer == 0) game.player1 else game.player2
        val opponent = if (game.currentPlayer == 0) game.player2 else game.player1

        val allowedPhases = listOf(
            GamePhase.DRAW_FROM_DECK,
            GamePhase.DRAW_FROM_PILE,

            GamePhase.PLAY_QUEEN,
            GamePhase.PLAY_JACK,
            GamePhase.PLAY_SEVEN_OR_EIGHT,
            GamePhase.PLAY_NINE_OR_TEN,
            //GamePhase.SWAP_OR_DISCARD
        )
        require(game.state in allowedPhases) {
            "Kartenauswahl ist in der Phase ${game.state} nicht erlaubt."
        }
        val isOwnCard = player.hand.flatten().contains(card)
        val isOpponentCard = opponent.hand.flatten().contains(card)


        when (game.state) {
            GamePhase.PLAY_JACK,
            GamePhase.PLAY_QUEEN -> require(isOwnCard || isOpponentCard) {
                "In dieser Phase darfst du eigene oder gegnerische Karten wählen."
            }
            GamePhase.PLAY_NINE_OR_TEN -> require(isOpponentCard) {
                "In dieser Phase darfst du nur gegnerische Karten wählen."}

            GamePhase.PLAY_SEVEN_OR_EIGHT -> require(isOwnCard) {
                "Mit 7 oder 8 darfst du nur eigene Karten ansehen."
            }

            else -> require(isOwnCard) {
                "Du darfst nur eigene Karten wählen."
            }
        }
        game.selected.clear()
        game.selected.add(card)

        game.log.add("${player.name} hat eine Karte ausgewählt: ${card.value}.")
        onAllRefreshables { refreshAfterSelect() }
    }

    fun confirmChoice()
    {
        val game = rootService.currentGame!!
        val selected = game.selected

        val player = if (game.currentPlayer == 0) game.player1 else game.player2
        val opponent = if (game.currentPlayer == 0) game.player2 else game.player1

        if (game.state==GamePhase.POWERCARD_DRAWN){
            playPowerCard()
        }
        when (game.state) {


            GamePhase.PLAY_QUEEN -> {
                require(selected.size == 2){ " SIZE 2." }
                require ((player.hand.flatten().contains(selected[0]) &&
                        opponent.hand.flatten().contains(selected[1]) ) ||
                        (player.hand.flatten().contains(selected[1]) &&
                                opponent.hand.flatten().contains(selected[0]) )){ "DIFFERENT OWNERS ." }
                confirmQueenShow(selected[0], selected[1])
            }

            GamePhase.confirmQueenShow -> {
                require(selected.size == 2){ " SIZE 2." }
                require ((player.hand.flatten().contains(selected[0]) &&
                        opponent.hand.flatten().contains(selected[1]) ) ||
                        (player.hand.flatten().contains(selected[1]) &&
                                opponent.hand.flatten().contains(selected[0]) )){ "DIFFERENT OWNERS ." }
                confirmQueenSwap()
                game.state = GamePhase.ENDTURN
            }

            GamePhase.PLAY_SEVEN_OR_EIGHT -> {
                require(selected.size == 1)
                require (player.hand.flatten().contains(selected[0]) ){ "I AM OWNER ." }
                playSevenOrEightEffect(selected[0])
                game.state = GamePhase.ENDTURN
            }

            GamePhase.PLAY_NINE_OR_TEN -> {
                require(selected.size == 1)
                require( opponent.hand.flatten().contains(selected[0]))
                { "Die ausgewählte Karte gehört nicht dem Gegner." }
                playNineOrTenEffect(selected[0])
                game.state = GamePhase.ENDTURN
            }

            GamePhase.PLAY_JACK -> {
                require(selected.size == 2){
                    " SIZE 2."
                }
                require ((player.hand.flatten().contains(selected[0]) &&
                        opponent.hand.flatten().contains(selected[1]) ) ||
                        (player.hand.flatten().contains(selected[1]) &&
                                opponent.hand.flatten().contains(selected[0]) )){ "DIFFERENT OWNERS ." }
                playJackEffect()
                game.state = GamePhase.ENDTURN
            }

            else -> throw IllegalStateException("In dieser Phase ist confirmChoice nicht erlaubt.")

        }
        onAllRefreshables { refreshAfterConfirmChoice() }
        if (game.state == GamePhase.ENDTURN) {
            rootService.gameService.endTurn()
        }
    }


    fun findCardPositionInHand(player: Player, card: Card): Pair<Int, Int>? {
        for (i in 0..1) {
            for (j in 0..1) {
                if (player.hand[i][j] == card) return i to j
            }
        }
        return null
    }
    private  fun playJackEffect() {
        val game = rootService.currentGame!!
        game.log.add("${currentPlayer().name} hat mit Bube blind eine Karte getauscht.")
        swapCard()

    }

    private fun confirmQueenShow(cardA: Card, cardB: Card) {
        val game = rootService.currentGame!!
        rootService.gameService.showCards(cardA, cardB)
        game.state = GamePhase.confirmQueenShow

    }


    fun confirmQueenSwap() {
        val game = rootService.currentGame!!
        require(game.state==GamePhase.confirmQueenShow) {
            "player saw the cards"
        }
        game.state = GamePhase.PLAY_QUEEN

        val player = currentPlayer()
        //val (cardA, cardB) = game.selected
        //swapCardsInternal(game, cardA, cardB)
        val drawn = player.drawnCard
        if (drawn != null) {
            game.playStack.push(drawn)
            player.drawnCard = null
        }
        game.log.add("${currentPlayer().name} hat nach Ansicht mit Dame getauscht.")
        swapCard()

    }


    private fun playSevenOrEightEffect(card: Card) {
        val game = rootService.currentGame
        checkNotNull(game) {"Kein aktives Spiel vorhanden."}
        rootService.gameService.showCards(card)

    }


    private fun playNineOrTenEffect(card: Card)  {
        val game = rootService.currentGame
        checkNotNull(game) {"Kein aktives Spiel vorhanden."}
        rootService.gameService.showCards(card)

    }

   fun knock() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active" }
        require(game.currentPlayer == 0 || game.currentPlayer == 1) {
            "Ungültiger Spieler-Index: ${game.currentPlayer}"

        }
        if (game.lastRound) return
       game.state = GamePhase.KNOCKED
       game.lastRound = true
       game.knockInitiatorIndex = game.currentPlayer
       game.log.add("${rootService.playerActionService.currentPlayer().name} klopft! Das Spiel endet nach diesem Zug.")
        onAllRefreshables { refreshAfterKnock() }

    }}


