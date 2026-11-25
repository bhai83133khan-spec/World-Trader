package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnemyBattleReportTest {
    @Test
    fun victoryLootAndReportAmountsMatchWalletDelta() {
        val city = CityData("port", "Port", "Rival", 100, 500, 10)
        val result = EnemyKingdomSystem.resolveBattle(
            "player", "Rival", 300, 40, city, 25, attackerIsPlayer = true, lootAvailable = 500
        )
        assertTrue(result.report.victory)
        assertEquals(500, result.report.loot)
        assertEquals(25 + result.report.loot, result.gold)
        assertEquals(20, result.report.defenderSurvivors)
        assertEquals(300 - result.report.attackerCasualties, result.report.attackerSurvivors)
    }

    @Test
    fun invalidYearDoesNotProduceDuplicateBattleReports() {
        val state = EnemyKingdomState(lastProcessedYear = 3)
        val result = EnemyKingdomSystem.advanceYear(state, emptyList(), 3, "Player")
        assertEquals(state, result.state)
        assertTrue(result.reports.isEmpty())
    }
}
