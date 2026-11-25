package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnemyKingdomSystemTest {
    private fun city(
        id: String,
        country: String,
        owned: Boolean = false,
        population: Int = 1_000,
        defense: Int = 10,
        controlledBy: String? = null
    ) = CityData(id, id, country, population, 1_000, defense, owned = owned, controlledBy = controlledBy)

    @Test
    fun initializeCreatesIndependentForeignKingdomsButNotPlayerKingdom() {
        val state = EnemyKingdomSystem.initialize(
            listOf(city("home", "Playerland", true, controlledBy = EnemyKingdomSystem.PLAYER_ID),
                city("a", "North"), city("b", "North"), city("c", "South")),
            "Playerland"
        )
        assertEquals(setOf("North", "South"), state.kingdoms.keys)
        assertTrue(state.kingdoms.getValue("North").army > 0)
        assertNotEquals(state.kingdoms.getValue("North").army, state.kingdoms.getValue("South").army)
    }

    @Test
    fun battleReportIncludesStrengthLossesSurvivorsLootAndCapture() {
        val result = EnemyKingdomSystem.resolveBattle(
            attacker = "player", defender = "Rival", attackerArmy = 300,
            defenderArmy = 40, city = city("port", "Rival", population = 100),
            gold = 25, attackerIsPlayer = true, lootAvailable = 500
        )
        val report = result.report
        assertTrue(report.victory)
        assertEquals(300, report.attackerStrength)
        assertEquals(50, report.defenderStrength)
        assertEquals(25, result.gold - 25)
        assertTrue(report.loot > 0)
        assertEquals(report.attackerStrength - report.attackerCasualties, report.attackerSurvivors)
        assertEquals(report.defenderStrength - 10, report.defenderSurvivors + ArmySystem.cityDefense(city("port", "Rival", population = 100)))
        assertTrue(report.capturedCity!!.owned)
        assertEquals(EnemyKingdomSystem.PLAYER_ID, report.capturedCity!!.controlledBy)
    }

    @Test
    fun losingBattleDoesNotLootOrCaptureAndHasSurvivors() {
        val city = city("home", "Playerland", owned = true, defense = 50,
            controlledBy = EnemyKingdomSystem.PLAYER_ID)
        val result = EnemyKingdomSystem.resolveBattle(
            "Rival", "Playerland", 20, 200, city, 0, false, 1_000
        )
        assertFalse(result.report.victory)
        assertEquals(0, result.report.loot)
        assertEquals(null, result.report.capturedCity)
        assertEquals(14, result.report.attackerSurvivors)
        assertTrue(result.report.defenderSurvivors > 0)
    }

    @Test
    fun yearlyAiGrowsAndOnlyProcessesEachYearOnce() {
        val cities = listOf(city("home", "Playerland", owned = true, defense = 100,
            controlledBy = EnemyKingdomSystem.PLAYER_ID), city("rival", "Rival", population = 1_000))
        val initial = EnemyKingdomSystem.initialize(cities, "Playerland")
        val first = EnemyKingdomSystem.advanceYear(initial, cities, 1, "Playerland")
        val repeated = EnemyKingdomSystem.advanceYear(first.state, first.cities, 1, "Playerland")
        assertTrue(first.state.kingdoms.getValue("Rival").army > initial.kingdoms.getValue("Rival").army)
        assertEquals(first.state, repeated.state)
        assertTrue(repeated.reports.isEmpty())
    }

    @Test
    fun aiTurnIsDeterministicAndCanCaptureWeakPlayerCity() {
        val home = city("home", "Playerland", owned = true, population = 100, defense = 1,
            controlledBy = EnemyKingdomSystem.PLAYER_ID)
        val enemy = city("enemy", "Rival", population = 20_000, defense = 1)
        val base = EnemyKingdomSystem.initialize(listOf(home, enemy), "Playerland")
        val forceful = base.copy(kingdoms = base.kingdoms.mapValues { (_, kingdom) ->
            kingdom.copy(army = 10_000, aggression = 100)
        })
        val one = EnemyKingdomSystem.advanceYear(forceful, listOf(home, enemy), 7, "Playerland")
        val two = EnemyKingdomSystem.advanceYear(forceful, listOf(home, enemy), 7, "Playerland")
        assertEquals(one, two)
        assertEquals(1, one.reports.size)
        assertTrue(one.reports.single().victory)
        assertFalse(one.cities.first { it.id == "home" }.owned)
        assertEquals("Rival", one.cities.first { it.id == "home" }.controlledBy)
    }
}
