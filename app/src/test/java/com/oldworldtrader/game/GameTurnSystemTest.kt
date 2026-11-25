package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTurnSystemTest {
    private fun city(
        id: String,
        country: String,
        population: Int,
        owned: Boolean = false,
        defense: Int = 10,
        controlledBy: String? = null,
        housingLevel: Int = 1,
        buildings: MutableMap<String, Int> = mutableMapOf()
    ) = CityData(id, id, country, population, 500, defense, owned, housingLevel, buildings,
        controlledBy = controlledBy)

    @Test
    fun advancingYearMigratesLegacyOwnershipGrowsCitiesAndSettlesKingdomIncome() {
        val home = city("home", "Playerland", 1_000, owned = true)
        val rival = city("rival", "Rival", 1_000)
        val result = GameTurnSystem.advanceYear(
            currentYear = 4, cities = listOf(home, rival), marketState = MarketState(),
            enemyKingdomState = EnemyKingdomState(), battleReports = emptyList(), army = 10,
            gold = 1_000, playerKingdom = "Playerland"
        )
        assertEquals(5, result.year)
        assertEquals("player", result.cities.first { it.id == "home" }.controlledBy)
        assertEquals(1_000, result.cities.first { it.id == "home" }.population) // Housing capacity prevents growth at the cap.
        assertEquals(50, result.cityIncome)
        assertEquals(1_040, result.gold)
        assertEquals(10, result.army)
        assertEquals(5, result.enemyKingdomState.lastProcessedYear)
    }

    @Test
    fun yearEndIsSafeAndDoesNotMutateSaveState() {
        val home = city("home", "Playerland", 100, owned = true)
        val state = EnemyKingdomState(lastProcessedYear = Int.MAX_VALUE)
        val result = GameTurnSystem.advanceYear(
            Int.MAX_VALUE, listOf(home), MarketState(), state, emptyList(), 3, 10,
            playerKingdom = "Playerland"
        )
        assertEquals(Int.MAX_VALUE, result.year)
        assertEquals(state, result.enemyKingdomState)
        assertEquals(10, result.gold)
        assertEquals(3, result.army)
    }

    @Test
    fun housingLimitsGrowthButNeverShrinksLegacyCityPopulation() {
        val small = city("small", "Playerland", 490, owned = true, housingLevel = 1)
        assertEquals(500, CityGrowthSystem.populationCapacity(small))
        assertEquals(10, CityGrowthSystem.annualGrowth(small))
        val full = small.copy(population = 500)
        assertEquals(0, CityGrowthSystem.annualGrowth(full))
        val legacy = small.copy(population = 3_000)
        assertEquals(3_000, CityGrowthSystem.populationCapacity(legacy))
        assertEquals(0, CityGrowthSystem.annualGrowth(legacy))
    }

    @Test
    fun enemyAttackIsResolvedIntoPersistentReportAndCaptureState() {
        val home = city("home", "Playerland", 100, owned = true, defense = 1,
            controlledBy = "player")
        val rival = city("rival", "Rival", 20_000, defense = 1)
        val initial = EnemyKingdomSystem.initialize(listOf(home, rival), "Playerland")
        val hostile = initial.copy(kingdoms = initial.kingdoms.mapValues { (_, faction) ->
            faction.copy(army = 10_000, aggression = 100)
        })
        val result = GameTurnSystem.advanceYear(
            6, listOf(home, rival), MarketState(), hostile, emptyList(), 100, 1_000,
            playerKingdom = "Playerland"
        )
        assertEquals(1, result.battleReports.size)
        assertTrue(result.battleReports.single().victory)
        assertFalse(result.cities.first { it.id == "home" }.owned)
        assertEquals("Rival", result.cities.first { it.id == "home" }.controlledBy)
    }
}
