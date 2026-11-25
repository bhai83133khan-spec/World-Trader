package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CityTravelSystemTest {
    private val cities = listOf(
        CityData("alex", "Alexandria", "Egypt", 1200, 6000, 20, owned = true),
        CityData("ven", "Venice", "Italy", 2400, 12000, 40, owned = false)
    )

    @Test
    fun unownedCityCanBeVisitedAndItsMarketSelected() {
        val result = CityTravelSystem.visit(cities[1], fleetAtSea = false)

        assertTrue(result.success)
        assertEquals("ven", result.cityId)
        assertTrue(result.message.contains("market"))
        assertFalse(cities[1].owned)
    }

    @Test
    fun cityTravelIsBlockedWhileFleetIsAtSea() {
        val result = CityTravelSystem.visit(cities[1], fleetAtSea = true)

        assertFalse(result.success)
        assertNull(result.cityId)
        assertTrue(result.message.contains("voyage"))
    }

    @Test
    fun invalidCityIdIsRejected() {
        val invalid = cities[1].copy(id = "")
        val result = CityTravelSystem.visit(invalid, fleetAtSea = false)

        assertFalse(result.success)
        assertNull(result.cityId)
    }

    @Test
    fun visitTargetUsesItsOwnPortIndexAndPricesDifferFromOtherCities() {
        val result = CityTravelSystem.visit(cities[1], fleetAtSea = false)
        assertEquals("ven", result.cityId)
        val destinationIndex = cities.indexOfFirst { it.id == result.cityId }
        val wheat = TradeGood("Wheat", 30)

        val alexPrice = MarketSystem.quote(wheat, cityIndex = 0, year = 1).price
        val venPrice = MarketSystem.quote(wheat, cityIndex = destinationIndex, year = 1).price

        assertNotEquals(alexPrice, venPrice)
    }

    @Test
    fun restoresSavedMarketCityWhenItStillExists() {
        assertEquals("ven", CityTravelSystem.restoreMarketCityId("ven", cities, "alex"))
    }

    @Test
    fun olderOrInvalidSaveFallsBackToFleetPort() {
        assertEquals("alex", CityTravelSystem.restoreMarketCityId("missing", cities, "alex"))
        assertEquals("alex", CityTravelSystem.restoreMarketCityId(null, cities, "alex"))
    }

    @Test
    fun invalidFleetPortFallsBackToFirstKnownCity() {
        assertEquals("alex", CityTravelSystem.restoreMarketCityId(null, cities, "missing"))
    }
}
