package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Test

class CityGrowthSystemTest {
    private fun city(
        population: Int,
        housingLevel: Int = 1,
        houseBuildingLevel: Int? = null
    ) = CityData(
        id = "growth-city",
        name = "Growth City",
        country = "Playerland",
        population = population,
        treasuryValue = 0,
        defense = 0,
        owned = true,
        housingLevel = housingLevel,
        buildings = houseBuildingLevel?.let { mutableMapOf("House" to it) } ?: mutableMapOf(),
        controlledBy = EnemyKingdomSystem.PLAYER_ID
    )

    @Test
    fun staleHouseBuildingEntryDoesNotSuppressLegacyHousingCapacity() {
        val city = city(population = 1_000, housingLevel = 5, houseBuildingLevel = 1)
        assertEquals(2_500, CityGrowthSystem.populationCapacity(city))
        assertEquals(30, CityGrowthSystem.annualGrowth(city))
    }

    @Test
    fun buildingHouseLevelsStillExpandCapacityWhenLegacyHousingIsLower() {
        val city = city(population = 1_000, housingLevel = 2, houseBuildingLevel = 4)
        assertEquals(2_500, CityGrowthSystem.populationCapacity(city))
        assertEquals(30, CityGrowthSystem.annualGrowth(city))
    }

    @Test
    fun negativeLegacyHousingLevelDoesNotOverflowIntoHugeCapacity() {
        val city = city(population = 100, housingLevel = Int.MIN_VALUE)
        assertEquals(500, CityGrowthSystem.populationCapacity(city))
        assertEquals(10, CityGrowthSystem.annualGrowth(city))
    }

    @Test
    fun houseCapacityIsClampedToIntRange() {
        val city = city(population = 100, housingLevel = Int.MAX_VALUE, houseBuildingLevel = Int.MAX_VALUE)
        assertEquals(Int.MAX_VALUE, CityGrowthSystem.populationCapacity(city))
    }

    @Test
    fun annualSimulationStopsPopulationAtHousingCapacity() {
        val city = city(population = 499, housingLevel = 1, houseBuildingLevel = 0)
        val result = GameTurnSystem.advanceYear(
            currentYear = 1,
            cities = listOf(city),
            marketState = MarketState(),
            enemyKingdomState = EnemyKingdomState(),
            battleReports = emptyList(),
            army = 0,
            gold = 100,
            playerKingdom = "Playerland"
        )
        assertEquals(2, result.year)
        assertEquals(500, result.cities.single().population)
    }

    @Test
    fun existingPopulationAboveBaseHousingCapacityIsNeverReducedOrGrown() {
        val city = city(population = 1_200, housingLevel = 1, houseBuildingLevel = 0)
        assertEquals(1_200, CityGrowthSystem.populationCapacity(city))
        assertEquals(0, CityGrowthSystem.annualGrowth(city))
    }
}
