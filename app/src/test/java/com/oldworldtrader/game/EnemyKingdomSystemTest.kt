package com.oldworldtrader.game

import org.junit.Assert.assertEquals
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
                CityData("legacyNorth", "Legacy North", "North", 9_000, 10_000, 100, owned = true),
                city("a", "North"), city("b", "North"), city("c", "South")),
            "Playerland"
        )
        assertEquals(setOf("North", "South"), state.kingdoms.keys)
        assertEquals(40, state.kingdoms.getValue("North").army) // Legacy-owned city must not inflate enemy forces.
        assertEquals(500, state.kingdoms.getValue("North").treasury) // Legacy-owned treasury must not seed the rival.
        assertEquals(40, state.kingdoms.getValue("South").army)
    }

    @Test
    fun eachKingdomGainsAnIncomeAndReinforcementsPerYear() {
        val cities = listOf(city("a", "North", population = 1_000))
        val state = EnemyKingdomSystem.initialize(cities, "Player")
        val result = EnemyKingdomSystem.advanceYear(state, cities, 1, "Player")
        assertEquals(42, result.state.kingdoms.getValue("North").army)
        assertEquals(538, result.state.kingdoms.getValue("North").treasury)
    }

    @Test
    fun legacyOwnedCityDoesNotBoostEnemyAnnualIncomeOrReinforcements() {
        val legacyOwned = CityData("legacy", "Legacy", "North", 9_000, 10_000, 100, owned = true)
        val neutral = city("north", "North", population = 1_000)
        val state = EnemyKingdomState(
            kingdoms = mapOf("North" to EnemyKingdom("North", army = 40, treasury = 500, aggression = 0))
        )

        val result = EnemyKingdomSystem.advanceYear(state, listOf(legacyOwned, neutral), 1, "Playerland")


        assertEquals(42, result.state.kingdoms.getValue("North").army)
        assertEquals(533, result.state.kingdoms.getValue("North").treasury)
        assertEquals(9_000, result.cities.first { it.id == "legacy" }.population)
    }

    @Test
    fun conqueredCitiesSeedAndGrowRivalKingdoms() {
        val captured = CityData("captured", "Captured", "Playerland", 5_000, 10_000, 10,
            owned = false, controlledBy = "North")
        val native = city("north", "North", population = 1_000)
        val holdings = listOf(captured, native)

        val initial = EnemyKingdomSystem.initialize(holdings, "Playerland")
        assertEquals(setOf("North"), initial.kingdoms.keys)
        assertEquals(40, initial.kingdoms.getValue("North").army)
        assertEquals(2_750, initial.kingdoms.getValue("North").treasury)

        val result = EnemyKingdomSystem.advanceYear(initial, holdings, 1, "Playerland")
        assertEquals(52, result.state.kingdoms.getValue("North").army) // +12 from 6,000 controlled people
        assertEquals(2_950, result.state.kingdoms.getValue("North").treasury) // +200 annual income
    }

    @Test
    fun duplicateYearIsIdempotent() {
        val cities = listOf(city("home", "Playerland", true, controlledBy = "player"), city("enemy", "Rival"))
        val initial = EnemyKingdomSystem.initialize(cities, "Playerland")
        val first = EnemyKingdomSystem.advanceYear(initial, cities, 1, "Playerland")
        val second = EnemyKingdomSystem.advanceYear(first.state, first.cities, 1, "Playerland")
        assertEquals(first, second)
    }
}
