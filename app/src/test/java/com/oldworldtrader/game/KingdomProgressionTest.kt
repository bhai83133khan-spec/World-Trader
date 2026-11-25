package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KingdomProgressionTest {
    private fun ownedCity(
        id: String,
        population: Int,
        buildings: Map<String, Int> = emptyMap()
    ) = CityData(
        id = id,
        name = id,
        country = "Egypt",
        population = population,
        treasuryValue = 0,
        defense = 0,
        owned = true,
        buildings = buildings.toMutableMap()
    )

    @Test
    fun emptyRealmStartsAsMerchantWithNoDiscountAndBaronAsNextRank() {
        val snapshot = KingdomProgression.evaluate(emptyList())
        assertEquals("Merchant", snapshot.currentRank.title)
        assertEquals(0, snapshot.currentRank.foundingDiscountPercent)
        assertEquals("Baron", snapshot.nextRank?.title)
        assertEquals(2_500, KingdomProgression.foundingCost(emptyList()))
    }

    @Test
    fun oneOwnedSettlementUnlocksBaronDiscount() {
        val snapshot = KingdomProgression.evaluate(listOf(ownedCity("new", 300)))
        assertEquals("Baron", snapshot.currentRank.title)
        assertEquals(5, snapshot.currentRank.foundingDiscountPercent)
        assertEquals(2_375, KingdomProgression.foundingCost(listOf(ownedCity("new", 300))))
        assertEquals("Count", snapshot.nextRank?.title)
    }

    @Test
    fun countRequiresCityPopulationAndBuildingMilestonesTogether() {
        val cities = listOf(
            ownedCity("one", 600, mapOf("House" to 1)),
            ownedCity("two", 400, mapOf("House" to 1))
        )
        assertEquals("Count", KingdomProgression.evaluate(cities).currentRank.title)
        assertEquals(10, KingdomProgression.evaluate(cities).currentRank.foundingDiscountPercent)
        assertEquals(2_250, KingdomProgression.foundingCost(cities))
        assertEquals("Duke", KingdomProgression.evaluate(cities).nextRank?.title)

        val missingPopulation = cities.map { it.copy(population = 300) }
        assertEquals("Baron", KingdomProgression.evaluate(missingPopulation).currentRank.title)
        val missingBuildings = cities.map { it.copy(buildings = mutableMapOf()) }
        assertEquals("Baron", KingdomProgression.evaluate(missingBuildings).currentRank.title)
    }

    @Test
    fun rankAdvancesToHighestTierOnlyWhenEveryRequirementIsMet() {
        val cities = listOf(
            ownedCity("a", 2_000, mapOf("House" to 2)),
            ownedCity("b", 2_000, mapOf("House" to 2)),
            ownedCity("c", 2_000, mapOf("House" to 2))
        )
        val snapshot = KingdomProgression.evaluate(cities)
        assertEquals("Duke", snapshot.currentRank.title)
        assertEquals("Kingdom", snapshot.nextRank?.title)
        assertEquals(15, snapshot.currentRank.foundingDiscountPercent)
    }

    @Test
    fun empireRequiresFiveOwnedCitiesTwentyFiveThousandPeopleAndEighteenLevels() {
        val fiveCities = (1..5).map { ownedCity("city$it", 5_000, mapOf("House" to 4)) }
        val snapshot = KingdomProgression.evaluate(fiveCities)
        assertEquals("Empire", snapshot.currentRank.title)
        assertNull(snapshot.nextRank)
        assertEquals(25, snapshot.currentRank.foundingDiscountPercent)
        assertEquals(1_875, KingdomProgression.foundingCost(fiveCities))
    }

    @Test
    fun unownedCityPopulationAndBuildingsDoNotCountTowardKingdomRank() {
        val cities = listOf(
            ownedCity("owned", 300),
            CityData("unowned", "unowned", "Italy", 50_000, 0, 0, owned = false,
                buildings = mutableMapOf("House" to 100))
        )
        assertEquals(KingdomMetrics(1, 300, 0), KingdomProgression.metrics(cities))
        assertEquals("Baron", KingdomProgression.evaluate(cities).currentRank.title)
    }

    @Test
    fun foundingCostClampsNegativeBaseAndUsesLongArithmetic() {
        val baron = listOf(ownedCity("one", 300))
        assertEquals(0, KingdomProgression.foundingCost(baron, -10))
        assertEquals(2_040_109_464, KingdomProgression.foundingCost(baron, Int.MAX_VALUE))
    }
}
