package service

import entity.Card

/**
 * A test implementation of the [Refreshable] interface that records which methods were called,
 * and stores the passed arguments for inspection in unit tests.
 */
class TestRefreshable: Refreshable {

    var refreshAfterGameStartCalled = false
    var refreshAfterShowCardsCalled = false
    var refreshAfterPlayPowerCalled = false
    var refreshAfterKnockCalled = false
    var refreshAfterTurnEndCalled = false
    var refreshAfterDrawCalled = false
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

    var lastCard1Shown: Card? = null
    var lastCard2Shown: Card? = null

    override fun refreshAfterGameStart() {
        refreshAfterGameStartCalled = true
    }

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

    override fun refreshAfterDraw() {
        refreshAfterDrawCalled = true
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

    /*override fun refreshAfterGameOver() {
        refreshAfterGameOverCalled = true
    }*/

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
        refreshAfterGameStartCalled = false
        refreshAfterShowCardsCalled = false
        refreshAfterPlayPowerCalled = false
        refreshAfterKnockCalled = false
        refreshAfterTurnEndCalled = false
        refreshAfterDrawCalled = false
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
        refreshAfterpassCalled = false
        lastCard1Shown = null
        lastCard2Shown = null
    }

}
