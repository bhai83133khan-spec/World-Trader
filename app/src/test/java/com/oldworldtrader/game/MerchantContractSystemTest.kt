package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MerchantContractSystemTest {
    @Test
    fun boardIsDeterministicAndVariesByPortAndYear() {
        assertEquals(MerchantContractSystem.board("alex", 2), MerchantContractSystem.board("alex", 2))
        assertNotEquals(MerchantContractSystem.board("alex", 2), MerchantContractSystem.board("ven", 2))
        assertNotEquals(MerchantContractSystem.board("alex", 1), MerchantContractSystem.board("alex", 2))
        assertTrue(MerchantContractSystem.board("alex", 2).map { it.id }.toSet().size > 1)
    }

    @Test
    fun invalidBoardInputsReturnNoOffers() {
        assertTrue(MerchantContractSystem.board("", 1).isEmpty())
        assertTrue(MerchantContractSystem.board("alex", 0).isEmpty())
    }

    @Test
    fun deliveryRequiresDestinationAndSufficientCargoWithoutMutatingInputs() {
        val contract = MerchantContractSystem.board("alex", 1).first()
        val cargo = mapOf(contract.goodName to contract.quantity)
        val wrongPort = MerchantContractSystem.complete(contract, "ven", 1, 500, cargo, MerchantContractState())
        assertFalse(wrongPort.success)
        assertEquals(500, wrongPort.gold)
        assertEquals(cargo, wrongPort.cargo)

        val shortCargo = MerchantContractSystem.complete(contract, "alex", 1, 500,
            mapOf(contract.goodName to contract.quantity - 1), MerchantContractState())
        assertFalse(shortCargo.success)
        assertEquals(500, shortCargo.gold)
    }

    @Test
    fun successfulDeliveryConsumesExactQuantityAndPaysOnce() {
        val contract = MerchantContractSystem.board("alex", 1).first()
        val startingCargo = mapOf(contract.goodName to contract.quantity + 3)
        val result = MerchantContractSystem.complete(contract, "alex", 1, 500, startingCargo, MerchantContractState())
        assertTrue(result.success)
        assertEquals(500 + contract.rewardGold, result.gold)
        assertEquals(3, result.cargo[contract.goodName])
        assertTrue(contract.id in result.state.completedContractIds)
        assertTrue(MerchantContractSystem.board("alex", 1, result.state).none { it.id == contract.id })
        val replay = MerchantContractSystem.complete(contract, "alex", 1, result.gold, result.cargo, result.state)
        assertFalse(replay.success)
        assertEquals(result.gold, replay.gold)
        assertEquals(result.cargo, replay.cargo)
    }

    @Test
    fun forgedOfferTermsCannotChangeRewardOrCargoRequirement() {
        val valid = MerchantContractSystem.board("alex", 1).first()
        val cargo = mapOf(valid.goodName to valid.quantity)
        val forged = valid.copy(rewardGold = valid.rewardGold + 1)
        val result = MerchantContractSystem.complete(forged, "alex", 1, 500, cargo, MerchantContractState())
        assertFalse(result.success)
        assertEquals(500, result.gold)
        assertEquals(cargo, result.cargo)
        assertFalse(valid.id in result.state.completedContractIds)
    }

    @Test
    fun completionHistoryIsBoundedAndKeepsNewestMarker() {
        val previous = (0 until MerchantContractState.MAX_SAVED_COMPLETIONS).map { "old-$it" }.toSet()
        val state = MerchantContractState(year = 1, completedContractIds = previous)
        val contract = MerchantContractSystem.board("alex", 1, state).first()
        val cargo = mapOf(contract.goodName to contract.quantity)
        val result = MerchantContractSystem.complete(contract, "alex", 1, 500, cargo, state)
        assertTrue(result.success)
        assertEquals(MerchantContractState.MAX_SAVED_COMPLETIONS, result.state.completedContractIds.size)
        assertTrue(contract.id in result.state.completedContractIds)
        assertFalse("old-0" in result.state.completedContractIds)

        val overLimit = (0 until 150).map { "contract-$it" }.toSet()
        val normalized = MerchantContractState(year = 1, completedContractIds = overLimit).forYear(1)
        assertEquals(MerchantContractState.MAX_SAVED_COMPLETIONS, normalized.completedContractIds.size)
        assertEquals((50 until 150).map { "contract-$it" }.toSet(), normalized.completedContractIds)
    }

    @Test
    fun completionRejectsOverflowAndMalformedContractsAtomically() {
        val valid = MerchantContract("c1", "alex", "Wheat", 5, 100, "Deliver wheat")
        val cargo = mapOf("Wheat" to 5)
        val overflow = MerchantContractSystem.complete(valid, "alex", 1, Int.MAX_VALUE - 50, cargo, MerchantContractState())
        assertFalse(overflow.success)
        assertEquals(Int.MAX_VALUE - 50, overflow.gold)
        assertEquals(cargo, overflow.cargo)

        assertFalse(MerchantContractSystem.complete(valid.copy(quantity = 0), "alex", 1, 100, cargo, MerchantContractState()).success)
        assertFalse(MerchantContractSystem.complete(valid.copy(rewardGold = -1), "alex", 1, 100, cargo, MerchantContractState()).success)
    }

    @Test
    fun completedBoardIsResetForNewYearAndRewardScalingIsBounded() {
        val contract = MerchantContractSystem.board("alex", 1).first()
        val result = MerchantContractSystem.complete(contract, "alex", 1, 0,
            mapOf(contract.goodName to contract.quantity), MerchantContractState())
        assertTrue(result.success)
        assertTrue(MerchantContractSystem.board("alex", 2, result.state).isNotEmpty())
        assertTrue(MerchantContractSystem.board("alex", Int.MAX_VALUE).all { it.rewardGold > 0 })
        assertEquals(MerchantContractState(year = 2), MerchantContractState(year = 1, completedContractIds = setOf("old")).forYear(2))
    }
}
