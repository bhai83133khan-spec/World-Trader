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
                city("a", "North"), city("b", "North"), city("c", "South")),
            "Playerland"
        )
        assertEquals(setOf("North", "South"), state.kingdoms.keys)
        assertTrue(state.kingdoms.getValue("North").army > 0)
        assertTrue(state.kingdoms.getValue("South").army > 0)
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
    fun duplicateYearIsIdempotent() {
        val cities = listOf(city("home", "Playerland", true, controlledBy = "player"), city("enemy", "Rival"))
        val initial = EnemyKingdomSystem.initialize(cities, "Playerland")
        val first = EnemyKingdomSystem.advanceYear(initial, cities, 1, "Playerland")
        val second = EnemyKingdomSystem.advanceYear(first.state, first.cities, 1, "Playerland")
        assertEquals(first, second)
    }
}
