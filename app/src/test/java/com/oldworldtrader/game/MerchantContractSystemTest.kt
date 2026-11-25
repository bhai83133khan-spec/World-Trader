package com.oldworldtrader.game

import org.junit.Assert.*
import org.junit.Test

class MerchantContractSystemTest {
    @Test fun boardIsDeterministicForPortAndYear() {
        assertEquals(MerchantContractSystem.board("alex", 2), MerchantContractSystem.board("alex", 2))
        assertNotEquals(MerchantContractSystem.board("alex", 2), MerchantContractSystem.board("ven", 2))
    }

    @Test fun deliveryRequiresDestinationPort() {
        val contract = MerchantContract("c1", "alex", "Wheat", 5, 100, "Deliver wheat")
        val result = MerchantContractSystem.complete(contract, "ven", 500, mapOf("Wheat" to 5))
        assertFalse(result.success)
        assertEquals(500, result.gold)
        assertEquals(5, result.cargo["Wheat"])
    }

    @Test fun deliveryRequiresEnoughCargoAndDoesNotMutateInput() {
        val contract = MerchantContract("c1", "alex", "Wheat", 5, 100, "Deliver wheat")
        val cargo = mapOf("Wheat" to 4)
        val result = MerchantContractSystem.complete(contract, "alex", 500, cargo)
        assertFalse(result.success)
        assertEquals(cargo, result.cargo)
    }

    @Test fun successfulDeliveryConsumesGoodsAndPaysReward() {
        val contract = MerchantContract("c1", "alex", "Wheat", 5, 100, "Deliver wheat")
        val result = MerchantContractSystem.complete(contract, "alex", 500, mapOf("Wheat" to 8))
        assertTrue(result.success)
        assertEquals(600, result.gold)
        assertEquals(3, result.cargo["Wheat"])
    }

    @Test fun completionRejectsGoldOverflowWithoutConsumingCargo() {
        val contract = MerchantContract("c1", "alex", "Wheat", 5, 100, "Deliver wheat")
        val cargo = mapOf("Wheat" to 5)
        val result = MerchantContractSystem.complete(contract, "alex", Int.MAX_VALUE - 50, cargo)
        assertFalse(result.success)
        assertEquals(Int.MAX_VALUE - 50, result.gold)
        assertEquals(cargo, result.cargo)
    }

    @Test fun yearlyRewardScalingIsBounded() {
        val early = MerchantContractSystem.board("alex", 1)
        val later = MerchantContractSystem.board("alex", 6)
        assertTrue(later.first().rewardGold >= early.first().rewardGold)
        assertTrue(MerchantContractSystem.board("alex", Int.MAX_VALUE).all { it.rewardGold > 0 })
    }
}
