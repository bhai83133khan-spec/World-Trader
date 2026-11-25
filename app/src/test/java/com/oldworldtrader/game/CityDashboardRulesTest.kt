package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class CityDashboardRulesTest {
    @Test
    fun dashboardProjectsSamePopulationGrowthAndIncomeAsYearAdvance() {
        val city = CityData("new", "New City", "Egypt", 300, 2500, 10, owned = true)

        assertEquals(EconomyLogic.populationGrowth(city), CityDashboardRules.annualGrowth(city))
        assertEquals(15, CityDashboardRules.nextYearIncome(city)) // 300 + 10 growth, then integer-divided by 20
    }

    @Test
    fun unownedCityHasNoProjectedGrowthOrIncome() {
        val city = CityData("ven", "Venice", "Italy", 2400, 12000, 40)

        assertEquals(0, CityDashboardRules.annualGrowth(city))
        assertEquals(0, CityDashboardRules.nextYearIncome(city))
        assertFalse(CityDashboardRules.upgradeBlockReason(city, EconomyLogic.buildingSpecs.first(), 12000) == null)
    }

    @Test
    fun upgradeReasonReportsPopulationFundsAndLevelLimits() {
        val house = EconomyLogic.buildingSpecs.first { it.name == "House" }
        val city = CityData("alex", "Alexandria", "Egypt", 0, 6000, 20, owned = true)

        assertEquals("Need 150 gold; you have 100.", CityDashboardRules.upgradeBlockReason(city, house, 100))
        assertNull(CityDashboardRules.upgradeBlockReason(city, house, 150))

        val maxed = city.copy(buildings = mutableMapOf("House" to Int.MAX_VALUE))
        assertEquals("Maximum level reached.", CityDashboardRules.upgradeBlockReason(maxed, house, Int.MAX_VALUE))
    }

    @Test
    fun urbanEngineeringDiscountChangesUpgradeAffordabilityAndErrorMessage() {
        val house = EconomyLogic.buildingSpecs.first { it.name == "House" }
        val city = CityData("alex", "Alexandria", "Egypt", 0, 6000, 20, owned = true)
        assertNull(CityDashboardRules.upgradeBlockReason(city, house, 135, 10))
        assertEquals("Need 135 gold; you have 134.", CityDashboardRules.upgradeBlockReason(city, house, 134, 10))
    }


    @Test
    fun dashboardSummariesReflectBuildingsAndOnlyTreatPositiveLevelsAsBuilt() {
        val city = CityData(
            "alex", "Alexandria", "Egypt", 1200, 6000, 20, owned = true,
            buildings = mutableMapOf("Hospital" to 1, "Park" to 0)
        )
        assertEquals("Good", CityDashboardRules.welfareLabel(city))
        assertEquals("Not available", CityDashboardRules.electricityLabel(city))

        val powered = city.copy(
            buildings = mutableMapOf("Hospital" to 1, "Park" to 1, "Power Plant" to 1),
            unlocked = mutableSetOf("Market", "Electricity")
        )
        assertEquals("Excellent", CityDashboardRules.welfareLabel(powered))
        assertEquals("Powered", CityDashboardRules.electricityLabel(powered))
    }

    @Test
    fun dashboardIncomePreviewIncludesMarketHarborAndWorkshopBonusesAfterGrowth() {
        val city = CityData(
            "alex", "Alexandria", "Egypt", 300, 6000, 20, owned = true,
            buildings = mutableMapOf("Market" to 2, "Harbor" to 1, "Workshop" to 1)
        )

        // 300 + 10 annual growth = 310 people => 15 base gold, plus 100 building income.
        assertEquals(115, CityDashboardRules.nextYearIncome(city))
    }

    @Test
    fun dashboardIncomePreviewRejectsRivalControlledOwnedCity() {
        val city = CityData(
            "alex", "Alexandria", "Egypt", 300, 6000, 20, owned = true,
            controlledBy = "Rival",
            buildings = mutableMapOf("Market" to 2)
        )

        assertEquals(0, CityDashboardRules.annualGrowth(city))
        assertEquals(0, CityDashboardRules.nextYearIncome(city))
    }

    @Test
    fun nextYearCityPreviewShowsServiceCostsAndNetContribution() {
        val city = CityData(
            "home", "Home", "Egypt", 1_200, 500, 10, owned = true,
            controlledBy = EnemyKingdomSystem.PLAYER_FACTION,
            buildings = mutableMapOf("Hospital" to 1, "Park" to 1)
        )
        assertEquals(60, CityDashboardRules.nextYearIncome(city))
        assertEquals(17, CityDashboardRules.nextYearCityUpkeep(city))
        assertEquals(43, CityDashboardRules.nextYearNetIncome(city))
    }

    @Test
    fun dashboardGrowthPreviewRespectsHousingCapacityAndUpgrade() {
        val nearCapacity = CityData("near", "Near Capacity", "Egypt", 510, 0, 0, owned = true)
        assertEquals(0, CityDashboardRules.annualGrowth(nearCapacity))
        assertEquals(25, CityDashboardRules.nextYearIncome(nearCapacity))

        val expandedHousing = nearCapacity.copy(housingLevel = 2)
        assertEquals(15, CityDashboardRules.annualGrowth(expandedHousing))
        assertEquals(26, CityDashboardRules.nextYearIncome(expandedHousing))
    }

}
