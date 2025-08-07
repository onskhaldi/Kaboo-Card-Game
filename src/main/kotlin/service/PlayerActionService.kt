package service
import entity.*
/**
 * Service layer class that provides the logic for the two possible actions a player
 * can take in War: drawing from the left stack or drawing from right stack.
 *
 * @param rootService The [RootService] instance to access the other service methods and entity layer
 */

class PlayerActionService (private val rootService: RootService) : AbstractRefreshingService() {
    /**
     * Returns the currently active player.
     */
    fun currentPlayer(): Player {
        val game = checkNotNull(rootService.currentGame) { "Kein Spiel aktiv." }
        return if (game.currentPlayer == 0) game.player1 else game.player2
    }

    /**
     * Executes the logic for playing a power card depending on its type.
     * Updates game state and logs the action.
     */
    fun playPowerCard() {
        val game = rootService.currentGame
        checkNotNull(game)
        require(game.state == GamePhase.POWERCARD_DRAWN) {
            "Powerkarten dürfen nur direkt nach dem Ziehen gespielt werden)."
        }
        require(game.currentPlayer == 1 || game.currentPlayer == 0)
        { "There is no active player: $game expected 1 or 0" }

        val player = if (game.currentPlayer == 0) game.player1 else game.player2
        val gegner = if (game.currentPlayer == 0) game.player2 else game.player1

        val card = player.drawnCard
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

            game.playStack.push(card)

        onAllRefreshables { refreshAfterPlayPower() }
    }

    /**
     * Executes a card swap, either with own field or with opponent based on the current phase.
     */
    fun swapCard() {
        val game = rootService.currentGame
        checkNotNull(game)
        val player = if (game.currentPlayer == 0) game.player1 else game.player2
        val gegner = if (game.currentPlayer == 0) game.player2 else game.player1
        when (game.state) {
            GamePhase.POWERCARD_DRAWN,
            GamePhase.PUNKTCARD_DRAWN,
            GamePhase.DRAW_FROM_PILE -> {
                if (game.selected.size == 1 && player.hand.flatten().contains(game.selected[0])) {
                    swapWithOwnField(game, player)
                } else {
                    throw IllegalStateException("Zum Tauschen muss genau eine eigene Karte ausgewählt werden.")
                }
            }

            GamePhase.PLAY_QUEEN, GamePhase.PLAY_JACK -> {
                swapCardsInternal(game, player, gegner)
            }

            else -> throw IllegalStateException("In Phase ${game.state} darf nicht getauscht werden.")
        }
    }

    /**
     * Internal logic for swapping cards between two players (for QUEEN and JACK powercards).
     */
    private fun swapCardsInternal(game: KabooGame, player: Player, gegner: Player) {
        require(
            game.state == GamePhase.PLAY_QUEEN || game.state == GamePhase.PLAY_JACK ||
                    game.state == GamePhase.CONFIRMQUEENSHOW
        ) {
            "Karten dürfen nur mit Dame oder Bube getauscht werden."
        }
        var posCurrentPlayer: Pair<Int, Int>? = null
        var posOppPlayer: Pair<Int, Int>? = null


        if (player.hand.flatten().contains(game.selected[0])) {

            posCurrentPlayer = findCardPositionInHand(player, game.selected[0])
            posOppPlayer = findCardPositionInHand(gegner, game.selected[1])
        } else {
            posCurrentPlayer = findCardPositionInHand(player, game.selected[1])
            posOppPlayer = findCardPositionInHand(gegner, game.selected[0])

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
        ) {
            "Tauschen ist nur direkt nach dem Ziehen einer Karte erlaubt."
        }

        val drawncard = player.drawnCard
        val selectedCard = game.selected.firstOrNull()
        requireNotNull(drawncard) { "Keine gezogene Karte vorhanden." }
        requireNotNull(selectedCard) { "Keine Karte ausgewählt." }

        val pos = findCardPositionInHand(player, selectedCard)
        requireNotNull(pos) { "Karte nicht im Handfeld gefunden." }

        val (row, col) = pos
        game.playStack.push(player.hand[row][col]) // alte Karte kommt auf den Ablagestapel
        player.hand[row][col] = drawncard          // gezogene Karte kommt ins Feld
        player.drawnCard = null

        game.log.add("${player.name} hat ${drawncard.value} mit seine karte getauscht.")
        onAllRefreshables { refreshAfterSwap() }
        game.state = GamePhase.ENDTURN
        rootService.gameService.endTurn()
    }

    /**
     * Handles drawing a card from the draw pile.
     * If it's a powercard, transitions to the respective state.
     */
    fun drawFromDeck() {
        val game = rootService.currentGame
        checkNotNull(game)

        if (game.drawPile.isEmpty()) {
            rootService.gameService.endTurn()
        }

        require(game.state == GamePhase.READYTODRAW) {
            "you must draw"
        }

        val card = game.drawPile.pop()

        currentPlayer().drawnCard = card

        game.state = GamePhase.DRAW_FROM_DECK
        if (card.isPowerCard()) {
            card.isPowercard = true
            game.log.add("${currentPlayer().name} hat eine Powerkarte (${card.value}) vom Nachziehstapel gezogen.")
            game.state = GamePhase.POWERCARD_DRAWN


        } else {
            game.log.add("${currentPlayer().name} hat eine Punktekarte (${card.value}) vom Nachziehstapel gezogen.")
            game.state = GamePhase.PUNKTCARD_DRAWN
        }

        if (game.drawPile.isEmpty()) {
            game.lastRound = true
        }


        onAllRefreshables { refreshAfterDrawDeck() }
    }

    /**
     * Handles drawing the top card from the play/discard pile.
     */
    fun drawFromPile() {
        val game = rootService.currentGame
        checkNotNull(game)
        require(game.playStack.isNotEmpty()) {
            "Der Ablagestapel ist leer – es kann keine Karte gezogen werden."
        }
        require(game.state == GamePhase.READYTODRAW) {
            "you must draw"
        }

        val topCard = game.playStack.pop()
        currentPlayer().drawnCard = topCard
        game.state = GamePhase.DRAW_FROM_PILE
        game.log.add("${currentPlayer().name} hat die oberste Karte vom Ablagestapel gezogen: ${topCard.value}.")
        onAllRefreshables { refreshAfterDrawPile() }
    }

    /**
     * Selects a card depending on the current game phase and the player.
     * Enforces valid selection rules.
     */

    fun selectCard(card: Card) {
        val game = rootService.currentGame
        checkNotNull(game) { "Kein aktives Spiel vorhanden." }

        val player = if (game.currentPlayer == 0) game.player1 else game.player2
        val opponent = if (game.currentPlayer == 0) game.player2 else game.player1

        val isOwnCard = player.hand.flatten().contains(card)
        val isOpponentCard = opponent.hand.flatten().contains(card)


        val allowedPhases = listOf(
            GamePhase.DRAW_FROM_DECK,
            GamePhase.DRAW_FROM_PILE,
            GamePhase.PLAY_QUEEN,
            GamePhase.PLAY_JACK,
            GamePhase.PLAY_SEVEN_OR_EIGHT,
            GamePhase.PLAY_NINE_OR_TEN,
            GamePhase.POWERCARD_DRAWN, GamePhase.PUNKTCARD_DRAWN
        )
        require(game.state in allowedPhases) {
            "Kartenauswahl ist in der Phase ${game.state} nicht erlaubt."
        }

        when (game.state) {

            GamePhase.PLAY_JACK, GamePhase.PLAY_QUEEN -> {
                require(isOwnCard || isOpponentCard) {
                    "In dieser Phase darfst du eigene oder gegnerische Karten wählen."
                }

                if (game.selected.contains(card)) return

                require(game.selected.size < 2) {
                    "Du darfst nur zwei Karten wählen."
                }

                if (game.selected.size == 1) {
                    val first = game.selected[0]
                    val valid =
                        (player.hand.flatten().contains(first) && opponent.hand.flatten().contains(card)) ||
                                (opponent.hand.flatten().contains(first) && player.hand.flatten().contains(card))
                    require(valid) {
                        "Du musst eine eigene und eine gegnerische Karte wählen."
                    }
                }


                game.selected.add(card)
            }

            GamePhase.PLAY_SEVEN_OR_EIGHT -> {
                require(isOwnCard) {
                    "Mit 7 oder 8 darfst du nur eigene Karten ansehen."
                }
                game.selected.clear()
                game.selected.add(card)
            }

            GamePhase.PLAY_NINE_OR_TEN -> {
                require(isOpponentCard) {
                    "In dieser Phase darfst du nur gegnerische Karten wählen."
                }
                game.selected.clear()
                game.selected.add(card)
            }

            else -> {
                // z.B. DRAW_FROM_DECK etc.
                require(isOwnCard) {
                    "Du darfst nur eigene Karten wählen."
                }
                game.selected.clear()
                game.selected.add(card)
            }
        }

        game.log.add("${player.name} hat eine Karte ausgewählt")
        onAllRefreshables { refreshAfterSelect() }
    }

    /**
     * Confirms the current action depending on game phase.
     * May lead to showing, swapping, or ending the turn.
     */

    fun confirmChoice() {
        val game = rootService.currentGame
        checkNotNull(game)
        val selected = game.selected


        val player = if (game.currentPlayer == 0) game.player1 else game.player2
        val opponent = if (game.currentPlayer == 0) game.player2 else game.player1

        if (game.state == GamePhase.POWERCARD_DRAWN) {
            playPowerCard()
            return
        }
        if (game.state == GamePhase.POWERCARD_DRAWN) {
            println("Powerkarte wurde gezogen, bitte zuerst Power-Effekt ausführen!")

        }
        when (game.state) {


            GamePhase.PLAY_QUEEN -> {
                require(selected.size == 2) { " SIZE 2." }
                require(
                    (player.hand.flatten().contains(selected[0]) &&
                            opponent.hand.flatten().contains(selected[1])) ||
                            (player.hand.flatten().contains(selected[1]) &&
                                    opponent.hand.flatten().contains(selected[0]))
                ) { "DIFFERENT OWNERS ." }
                confirmQueenShow(selected[0], selected[1])
                game.state = GamePhase.PLAY_QUEEN
                game.state = GamePhase.CONFIRMQUEENSHOW
            }

            GamePhase.CONFIRMQUEENSHOW -> {
                confirmQueenSwap()

            }

            GamePhase.PLAY_SEVEN_OR_EIGHT -> {
                require(selected.size == 1)
                require(player.hand.flatten().contains(selected[0])) { "I AM OWNER ." }

                val drawn = player.drawnCard
                if (drawn != null) {
                    game.playStack.push(drawn)
                    player.drawnCard = null
                }
                playSevenOrEightEffect(selected[0])
                game.selected.clear()
                game.state = GamePhase.ENDTURN
            }

            GamePhase.PLAY_NINE_OR_TEN -> {
                require(selected.size == 1)
                require(opponent.hand.flatten().contains(selected[0]))
                { "Die ausgewählte Karte gehört nicht dem Gegner." }

                val drawn = player.drawnCard
                if (drawn != null) {
                    game.playStack.push(drawn)
                    player.drawnCard = null
                }
                playNineOrTenEffect(selected[0])
                game.selected.clear()
                game.state = GamePhase.ENDTURN
            }

            GamePhase.PLAY_JACK -> {
                require(selected.size == 2) {
                    " SIZE 2."
                }
                require(
                    (player.hand.flatten().contains(selected[0]) &&
                            opponent.hand.flatten().contains(selected[1])) ||
                            (player.hand.flatten().contains(selected[1]) &&
                                    opponent.hand.flatten().contains(selected[0]))
                ) { "DIFFERENT OWNERS ." }
                val drawn = player.drawnCard
                if (drawn != null) {
                    game.playStack.push(drawn)
                    player.drawnCard = null
                }
                playJackEffect()
                game.selected.clear()
                game.state = GamePhase.ENDTURN
            }

            else -> throw IllegalStateException("In dieser Phase ist confirmChoice nicht erlaubt.")
        }
        onAllRefreshables { refreshAfterConfirmChoice() }
        if (game.state == GamePhase.ENDTURN) {
            rootService.gameService.endTurn()
        }
    }


    /**
     * Finds the (row, column) of a given card in the player’s hand.
     */
    fun findCardPositionInHand(player: Player, card: Card): Pair<Int, Int>? {
        for (i in 0..1) {
            for (j in 0..1) {
                if (player.hand[i][j] == card) return i to j
            }
        }
        return null
    }

    /**
     * Executes JACK effect: blind swap of cards between players.
     */
    private fun playJackEffect() {
        val game = rootService.currentGame
        checkNotNull(game)
        val actingPlayer = currentPlayer() // capture before swap
        swapCard()
        game.log.add("${actingPlayer.name} hat mit Bube blind eine Karte getauscht.")
    }

    /**
     * Executes QUEEN card effect: show 2 cards (own and opponent), then confirm swap.
     */
    private fun confirmQueenShow(cardA: Card, cardB: Card) {
        val game = rootService.currentGame
        checkNotNull(game)
        rootService.gameService.showCards(cardA, cardB)
        game.state = GamePhase.CONFIRMQUEENSHOW

    }

    /**
     * Finalizes the QUEEN swap after the cards have been viewed.
     */
    fun confirmQueenSwap() {
        val game = rootService.currentGame
        checkNotNull(game)
        require(game.state == GamePhase.CONFIRMQUEENSHOW) {
            "player saw the cards"
        }

        val player = currentPlayer()
        val gegner = if (game.currentPlayer == 0) game.player2 else game.player1


        val drawn = player.drawnCard
        if (drawn != null) {
            game.playStack.push(drawn)
            player.drawnCard = null
        }

        swapCardsInternal(game, player, gegner)

        game.selected.clear()
        game.log.add("${player.name} hat nach Ansicht mit Dame getauscht.")
    }

    /**
     * Cancels the current power card effect, specifically during the Queen's
     * "show cards" confirmation phase.
     *
     * This method:
     * - Verifies that the current game phase is [GamePhase.CONFIRMQUEENSHOW].
     * - Sets the game phase to [GamePhase.ENDTURN].
     * - Calls [rootService.gameService.endTurn] to advance to the next player's turn.
     * - Logs the cancellation action to the game log.
     * - Notifies all registered refreshables via [refreshAfterConfirmChoice] to update the UI.
     *
     * @throws IllegalArgumentException if the current game phase is not [GamePhase.CONFIRMQUEENSHOW].
     */
    fun cancelPowerEffect() {
        val game = rootService.currentGame ?: return
        require(game.state == GamePhase.CONFIRMQUEENSHOW) {
            "player saw the cards"
        }

        game.state = GamePhase.ENDTURN
        rootService.gameService.endTurn()
        game.log.add("${currentPlayer().name} hat den Tausch abgebrochen.")
        onAllRefreshables { refreshAfterConfirmChoice() }
    }

    /**
     * Executes SEVEN or EIGHT effect: reveals a selected own card.
     */
    private fun playSevenOrEightEffect(card: Card) {
        val game = rootService.currentGame
        checkNotNull(game) { "Kein aktives Spiel vorhanden." }
        rootService.gameService.showCards(card)
    }

    /**
     * Executes NINE or TEN effect: reveals a selected opponent's card.
     */
    private fun playNineOrTenEffect(card: Card) {
        val game = rootService.currentGame
        checkNotNull(game) { "Kein aktives Spiel vorhanden." }
        rootService.gameService.showCards(card)
    }

    /**
     * Triggers a knock to initiate the final round of the game.
     * Allowed only once before last round.
     */
    fun knock() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active" }
        require(game.currentPlayer == 0 || game.currentPlayer == 1) {
            "Ungültiger Spieler-Index: ${game.currentPlayer}"
        }
        if (game.lastRound) return  // nobody else may knock now

        val knockerName = rootService.playerActionService.currentPlayer().name
        game.lastRound = true
        game.knockInitiatorIndex = game.currentPlayer
        game.state = GamePhase.ENDTURN
        game.log.add("$knockerName klopft! Alle anderen Spieler haben noch genau einen Zug.")

        onAllRefreshables { refreshAfterKnock() }

       rootService.gameService.endTurn()
    }

}




