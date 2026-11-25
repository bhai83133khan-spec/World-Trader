package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResearchSystemTest {
    @Test
    fun tradeGuildCanBeResearchedAtStartAndDeductsCostAtomically() {
        val result = ResearchSystem.research(ResearchState(), ResearchSystem.TRADE_GUILD, 1_200, 0L)
        assertTrue(result.success)
        assertEquals(0, result.gold)
        assertTrue(ResearchSystem.isResearched(result.state, ResearchSystem.TRADE_GUILD))
        assertEquals(110, ResearchSystem.salePrice(100, result.state))
    }

    @Test
    fun insufficientFundsLeaveResearchAndWalletUnchanged() {
        val initial = ResearchState()
        val result = ResearchSystem.research(initial, ResearchSystem.TRADE_GUILD, 1_199, 0L)
        assertFalse(result.success)
        assertEquals(1_199, result.gold)
        assertEquals(initial, result.state)
    }

    @Test
    fun unknownAndDuplicateResearchAreRejectedWithoutMutation() {
        val tradeGuild = ResearchSystem.research(ResearchState(), ResearchSystem.TRADE_GUILD, 2_000, 0L)
        assertFalse(ResearchSystem.research(tradeGuild.state, "unknown", 2_000, 0L).success)
        val duplicate = ResearchSystem.research(tradeGuild.state, ResearchSystem.TRADE_GUILD, 2_000, 0L)
        assertFalse(duplicate.success)
        assertEquals(tradeGuild.state, duplicate.state)
        assertEquals(2_000, duplicate.gold)
    }

    @Test
    fun shipwrightRequiresTradeGuildAndEightHundredOwnedPopulation() {
        val initial = ResearchState()
        assertFalse(ResearchSystem.research(initial, ResearchSystem.SHIPWRIGHT, 10_000, 5_000L).success)
        val tradeGuild = ResearchSystem.research(initial, ResearchSystem.TRADE_GUILD, 10_000, 0L).state
        assertFalse(ResearchSystem.research(tradeGuild, ResearchSystem.SHIPWRIGHT, 10_000, 799L).success)
        val shipwright = ResearchSystem.research(tradeGuild, ResearchSystem.SHIPWRIGHT, 10_000, 800L)
        assertTrue(shipwright.success)
        assertEquals(40, ResearchSystem.cargoCapacityBonus(shipwright.state))
    }

    @Test
    fun militaryDoctrineRequiresTradeGuildAndPopulationThreshold() {
        val tradeGuild = ResearchSystem.research(ResearchState(), ResearchSystem.TRADE_GUILD, 10_000, 0L).state
        assertFalse(ResearchSystem.research(tradeGuild, ResearchSystem.MILITARY_DOCTRINE, 10_000, 1_499L).success)
        val result = ResearchSystem.research(tradeGuild, ResearchSystem.MILITARY_DOCTRINE, 10_000, 1_500L)
        assertTrue(result.success)
        assertEquals(15, ResearchSystem.recruitDiscountPercent(result.state))
        assertEquals(85, ResearchSystem.recruitCost(100, result.state))
        assertEquals(425, ResearchSystem.recruitCost(500, result.state))
    }

    @Test
    fun urbanEngineeringRequiresTradeGuildAndPopulationThreshold() {
        val tradeGuild = ResearchSystem.research(ResearchState(), ResearchSystem.TRADE_GUILD, 10_000, 0L).state
        assertFalse(ResearchSystem.research(tradeGuild, ResearchSystem.URBAN_ENGINEERING, 10_000, 999L).success)
        val result = ResearchSystem.research(tradeGuild, ResearchSystem.URBAN_ENGINEERING, 10_000, 1_000L)
        assertTrue(result.success)
        assertEquals(10, ResearchSystem.buildingUpgradeDiscountPercent(result.state))
        assertEquals(630, ResearchSystem.applyDiscount(700, ResearchSystem.buildingUpgradeDiscountPercent(result.state)))
    }

    @Test
    fun saleBonusDoesNotApplyBeforeTradeGuildAndClampsOverflow() {
        assertEquals(100, ResearchSystem.salePrice(100, ResearchState()))
        val guild = ResearchSystem.research(ResearchState(), ResearchSystem.TRADE_GUILD, 1_200, 0L).state
        assertEquals(110, ResearchSystem.salePrice(100, guild))
        assertEquals(Int.MAX_VALUE, ResearchSystem.salePrice(Int.MAX_VALUE, guild))
    }

    @Test
    fun discountsAreBoundedAndDoNotTurnPositiveCostsFree() {
        assertEquals(0, ResearchSystem.applyDiscount(0, 50))
        assertEquals(0, ResearchSystem.applyDiscount(-1, 50))
        assertEquals(1, ResearchSystem.applyDiscount(1, 99))
        assertEquals(1, ResearchSystem.applyDiscount(1, 100))
        assertEquals(0, ResearchSystem.applyDiscount(100, -20))
        assertEquals(1, ResearchSystem.applyDiscount(100, 200))
    }

    @Test
    fun restoreFiltersUnknownIdsAndKeepsValidResearch() {
        val restored = ResearchSystem.restore(setOf(ResearchSystem.TRADE_GUILD, "bad-id", ResearchSystem.SHIPWRIGHT))
        assertEquals(setOf(ResearchSystem.TRADE_GUILD, ResearchSystem.SHIPWRIGHT), restored.researchedTechIds)
        assertEquals(40, ResearchSystem.cargoCapacityBonus(restored))
    }
}
