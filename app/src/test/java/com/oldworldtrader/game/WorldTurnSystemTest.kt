package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldTurnSystemTest {
    @Test
    fun migratesLegacyOwnedCitiesAndLeavesEnemyCitiesNeutral() {
        val legacy = CityData("old", "Old", "Italy", 1000, 500, 10, owned = true)
        val enemy = CityData("enemy", "Enemy", "Egypt", 2000, 700, 20)
        val migrated = WorldTurnSystem.migrateOwnership(listOf(legacy, enemy))
        assertEquals(EnemyKingdomSystem.PLAYER_ID, migrated[0].controlledBy)
        assertFalse(migrated[1].owned)
        assertEquals(null, migrated[1].controlledBy)
    }

    @Test
    fun kingdomTaxesArePopulationAndBuildingBasedAndBounded() {
        val city = CityData("a", "A", "Egypt", 2_000, 500, 10, owned = true,
            controlledBy = EnemyKingdomSystem.PLAYER_ID,
            buildings = mutableMapOf("Market" to 2, "Harbor" to 1))
        assertEquals(175, WorldTurnSystem.ownedCityIncome(city, 10))
        assertEquals(350, WorldTurnSystem.ownedCityIncome(city, 20))
        assertEquals(0, WorldTurnSystem.ownedCityIncome(city, -10))
        assertEquals(0, WorldTurnSystem.ownedCityIncome(city.copy(controlledBy = "Rival"), 10))
    }

    @Test
    fun conqueredTerritoryNoLongerPaysPlayerIncome() {
        val city = CityData("a", "A", "Egypt", 2_000, 500, 10, owned = true,
            controlledBy = "Rival")
        assertEquals(0, WorldTurnSystem.kingdomIncome(listOf(city)))
        assertTrue(WorldTurnSystem.kingdomIncome(listOf(city.copy(controlledBy = "player"))) > 0)
    }
}
