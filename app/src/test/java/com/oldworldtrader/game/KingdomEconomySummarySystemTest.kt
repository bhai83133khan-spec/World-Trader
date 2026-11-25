package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Test

class KingdomEconomySummarySystemTest {
    @Test
    fun aggregatesOnlyPlayerCitiesAndBreaksDownAnnualIncomeAndUpkeep() {
        val home = CityData("home", "Home", "Egypt", 1_000, 1_000, 10, owned = true,
            buildings = mutableMapOf("Market" to 2))
        val port = CityData("port", "Port", "Italy", 500, 1_000, 10, owned = true,
            buildings = mutableMapOf("Harbor" to 1))
        val rival = CityData("rival", "Rival", "Spain", 50_000, 1_000, 10, owned = true,
            controlledBy = "Rival", buildings = mutableMapOf("Market" to 100))
        val unowned = CityData("unowned", "Unowned", "India", 90_000, 1_000, 10, owned = false)
        val summary = KingdomEconomySummarySystem.evaluate(
            listOf(home, port, rival, unowned), fieldArmy = 20,
            cityGarrisons = mapOf("home" to 10, "port" to 5, "rival" to 100, "missing" to 20)
        )

        assertEquals(2, summary.ownedCities)
        assertEquals(1_500L, summary.ownedPopulation)
        assertEquals(150, summary.grossAnnualIncome)
        assertEquals(18, summary.cityServiceUpkeep)
        assertEquals(20, summary.fieldArmyUpkeep)
        assertEquals(15, summary.garrisonUpkeep)
        assertEquals(97, summary.netAnnualBalance)
    }

    @Test
    fun clampsInvalidArmyAndOverCapacityGarrisonInNetBalance() {
        val city = CityData("home", "Home", "Egypt", 1_000, 1_000, 10, owned = true,
            buildings = mutableMapOf("Market" to 1))
        val summary = KingdomEconomySummarySystem.evaluate(listOf(city), fieldArmy = -50,
            cityGarrisons = mapOf("home" to 999))
        assertEquals(70, summary.grossAnnualIncome)
        assertEquals(5, summary.cityServiceUpkeep)
        assertEquals(0, summary.fieldArmyUpkeep)
        assertEquals(100, summary.garrisonUpkeep)
        assertEquals(-35, summary.netAnnualBalance)
    }
}
