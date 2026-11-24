package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CityUpgradeTest {
    @Test
    fun houseUpgradeOnlyAddsItsConfiguredPopulationAndHousingLevel() {
        val city = CityData("alex", "Alexandria", "Egypt", 1200, 6000, 20, true)
        val house = EconomyLogic.buildingSpecs.first { it.name == "House" }

        val upgraded = EconomyLogic.applyUpgrade(city, house)

        assertEquals(1280, upgraded.population)
        assertEquals(2, upgraded.housingLevel)
        assertEquals(1, upgraded.buildings["House"])
        assertEquals(1, city.housingLevel) // original remains unchanged
    }

    @Test
    fun nonHousingUpgradeDoesNotIncreaseHousingOrAddGenericPopulation() {
        val city = CityData("alex", "Alexandria", "Egypt", 1200, 6000, 20, true)
        val hospital = EconomyLogic.buildingSpecs.first { it.name == "Hospital" }

        val upgraded = EconomyLogic.applyUpgrade(city, hospital)

        assertEquals(1240, upgraded.population)
        assertEquals(1, upgraded.housingLevel)
        assertEquals(1, upgraded.buildings["Hospital"])
    }

    @Test
    fun zeroPopulationBoostBuildingLeavesPopulationAndHousingAlone() {
        val city = CityData("alex", "Alexandria", "Egypt", 1200, 6000, 20, true)
        val barracks = EconomyLogic.buildingSpecs.first { it.name == "Army Barracks" }

        val upgraded = EconomyLogic.applyUpgrade(city, barracks)

        assertEquals(1200, upgraded.population)
        assertEquals(1, upgraded.housingLevel)
        assertEquals(1, upgraded.buildings["Army Barracks"])
    }

    @Test
    fun powerPlantUnlocksElectricityWithoutMutatingOriginalUnlocks() {
        val city = CityData("alex", "Alexandria", "Egypt", 1500, 6000, 20, true)
        val power = EconomyLogic.buildingSpecs.first { it.name == "Power Plant" }

        val upgraded = EconomyLogic.applyUpgrade(city, power)

        assertTrue(upgraded.unlocked.contains("Electricity"))
        assertTrue(!city.unlocked.contains("Electricity"))
        assertEquals(1, upgraded.buildings["Power Plant"])
    }

    @Test
    fun upgradeLevelsAndPopulationAreCappedAtIntMax() {
        val city = CityData(
            "alex", "Alexandria", "Egypt", Int.MAX_VALUE, 6000, 20, true,
            housingLevel = Int.MAX_VALUE,
            buildings = mutableMapOf("House" to Int.MAX_VALUE)
        )
        val house = EconomyLogic.buildingSpecs.first { it.name == "House" }

        val upgraded = EconomyLogic.applyUpgrade(city, house)

        assertEquals(Int.MAX_VALUE, upgraded.population)
        assertEquals(Int.MAX_VALUE, upgraded.housingLevel)
        assertEquals(Int.MAX_VALUE, upgraded.buildings["House"])
    }
}
