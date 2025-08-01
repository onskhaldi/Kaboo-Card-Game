package service
import entity.Card
import entity.Player

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the GUI classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * GUI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable {
    fun refreshAfterpass(){}
    fun refreshAfterGameStart(){}
    fun refreshAfterShowCards(card1: Card, card2: Card?){}
    fun refreshAfterPlayPower(){}
    fun refreshAfterKnock(){}
    fun refreshAfterTurnEnd(){}
    fun refreshAfterDraw(){}
    fun refreshAfterSelect(){}
    fun refreshAfterSwap(){}
    fun refreshAfterConfirmSwap() {}
    fun refreshAfterPlayerEdit(){}
    fun refreshAfterDiscard(){}
    fun refreshAfterConfirmShown(){}
    fun refreshAfterStartTurn(){}
    fun refreshAfterGameOver(gewinner : Player?, score: Int){}
    fun refreshAfterHideCards(){}
    fun refreshAfterConfirmChoice(){}
    fun refreshAfterQuit() {}
    fun refreshAfterRestart(){}
}

