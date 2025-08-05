package service

import entity.Card
import entity.Player

/**
 * A test implementation of the [Refreshable] interface that records which methods were called,
 * and stores the passed arguments for inspection in unit tests.
 */
class TestRefreshable: Refreshable {
    var restartCalled=false
    var  quitCalled=false
    var refreshAfterHideStartingCardsCalled=false
    var refreshAfterShowStartingCardsCalled =false
    var refreshAfterGameStartCalled = false
    var refreshAfterShowCardsCalled = false
    var refreshAfterPlayPowerCalled = false
    var refreshAfterKnockCalled = false
    var refreshAfterTurnEndCalled = false
    var refreshAfterDrawFromDeckCalled = false
    var refreshAfterSelectCalled = false
    var refreshAfterSwapCalled = false
    var refreshAfterConfirmSwapCalled = false
    var refreshAfterPlayerEditCalled = false
    var refreshAfterDiscardCalled = false
    var refreshAfterConfirmShownCalled = false
    var refreshAfterStartTurnCalled = false
    var refreshAfterGameOverCalled = false
    var refreshAfterHideCardsCalled = false
    var refreshAfterConfirmChoiceCalled = false
    var refreshAfterpassCalled = false
var refreshAfterDrawPileCalled=false
    var lastCard1Shown: Card? = null
    var lastCard2Shown: Card? = null

    override fun  refreshAfterQuit(){
        quitCalled=true
    }

    override fun refreshAfterStartNewGame() {
        refreshAfterGameStartCalled = true
    }
    override fun refreshAfterGameOver(gewinner: Player?, score: Int)
    { refreshAfterGameOverCalled=true}

    override fun refreshAfterShowCards(card1: Card, card2: Card?) {
        refreshAfterShowCardsCalled = true
        lastCard1Shown = card1
        lastCard2Shown = card2
    }

    override fun refreshAfterPlayPower() {
        refreshAfterPlayPowerCalled = true
    }

    override fun refreshAfterKnock() {
        refreshAfterKnockCalled = true
    }

    override fun refreshAfterTurnEnd() {
        refreshAfterTurnEndCalled = true
    }

    override fun refreshAfterDrawDeck() {
        refreshAfterDrawFromDeckCalled=true
    }
    override fun refreshAfterRestart() {
        restartCalled = true
    }
override fun refreshAfterDrawPile(){
    refreshAfterDrawPileCalled=true
}
    override fun refreshAfterSelect() {
        refreshAfterSelectCalled = true
    }

    override fun refreshAfterSwap() {
        refreshAfterSwapCalled = true
    }

    override fun refreshAfterConfirmSwap() {
        refreshAfterConfirmSwapCalled = true
    }

    override fun refreshAfterPlayerEdit() {
        refreshAfterPlayerEditCalled = true
    }


    override fun refreshAfterDiscard() {
        refreshAfterDiscardCalled = true
    }

    override fun refreshAfterConfirmShown() {
        refreshAfterConfirmShownCalled = true
    }

    override fun refreshAfterStartTurn() {
        refreshAfterStartTurnCalled=true
    }

    override fun  refreshAfterShowStartingCards(){
        refreshAfterShowStartingCardsCalled=true
    }

    override fun refreshAfterHideStartingCards() {
        refreshAfterHideStartingCardsCalled = true
    }


    override fun refreshAfterHideCards() {
        refreshAfterHideCardsCalled = true
    }

    override fun refreshAfterConfirmChoice() {
        refreshAfterConfirmChoiceCalled = true
    }

    override fun refreshAfterpass() {
        refreshAfterpassCalled = true
    }
    fun reset() {
        refreshAfterGameOverCalled=false
        refreshAfterGameStartCalled = false
        refreshAfterShowCardsCalled = false
        refreshAfterPlayPowerCalled = false
        refreshAfterKnockCalled = false
        refreshAfterTurnEndCalled = false
        refreshAfterDrawFromDeckCalled = false
        refreshAfterSelectCalled = false
        refreshAfterSwapCalled = false
        refreshAfterConfirmSwapCalled = false
        refreshAfterPlayerEditCalled = false
        refreshAfterDiscardCalled = false
        refreshAfterConfirmShownCalled = false
        refreshAfterStartTurnCalled = false
        refreshAfterGameOverCalled = false
        refreshAfterHideCardsCalled = false
        refreshAfterConfirmChoiceCalled = false
        refreshAfterHideStartingCardsCalled=false
         refreshAfterShowStartingCardsCalled=false
        refreshAfterDrawPileCalled=false
        refreshAfterpassCalled = false
        lastCard1Shown = null
        lastCard2Shown = null
         quitCalled=false
        var restartCalled=false
    }

}

