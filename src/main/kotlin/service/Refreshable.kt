package service

import entity.Card
import entity.Player

/**
 * Interface for notifying observers (usually GUI classes) about changes in the game state.
 *
 * All methods have empty default implementations so implementers only need to override
 * the events they care about.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable {

    /** Called after both players' starting cards are revealed. */
    fun refreshAfterShowStartingCards() {}

    /** Called after both players' starting cards are hidden again. */
    fun refreshAfterHideStartingCards() {}

    /** Called when the draw pile changes (e.g., after reshuffling). */
    fun refreshAfterDrawPile() {}

    /** Called after a new game has been started. */
    fun refreshAfterStartNewGame() {}

    /** Called after a player passes their turn without drawing. */
    fun refreshAfterpass() {}

    /** Called when one or two cards should be revealed to the player(s). */
    fun refreshAfterShowCards(card1: Card, card2: Card?) {}

    /** Called after a power card has been played. */
    fun refreshAfterPlayPower() {}

    /** Called when a player knocks. */
    fun refreshAfterKnock() {}

    /** Called when a player's turn ends. */
    fun refreshAfterTurnEnd() {}

    /** Called after drawing a card from the deck. */
    fun refreshAfterDrawDeck() {}

    /** Called after a card has been selected. */
    fun refreshAfterSelect() {}

    /** Called after swapping one or more cards. */
    fun refreshAfterSwap() {}

    /** Called after confirming a swap action. */
    fun refreshAfterConfirmSwap() {}

    /** Called after editing a player's name or properties. */
    fun refreshAfterPlayerEdit() {}

    /** Called after a card is discarded. */
    fun refreshAfterDiscard() {}

    /** Called after confirming a shown card. */
    fun refreshAfterConfirmShown() {}

    /** Called after a new turn starts. */
    fun refreshAfterStartTurn() {}

    /**
     * Called when the game is over.
     * @param gewinner The winning player, or `null` for a tie.
     * @param score The final score of the winning player.
     */
    fun refreshAfterGameOver(gewinner: Player?, score: Int) {}

    /** Called after hiding any temporarily revealed cards. */
    fun refreshAfterHideCards() {}

    /** Called after confirming a choice during a special effect or action. */
    fun refreshAfterConfirmChoice() {}

    /** Called after quitting the current game. */
    fun refreshAfterQuit() {}

    /** Called after restarting the game. */
    fun refreshAfterRestart() {}
}
