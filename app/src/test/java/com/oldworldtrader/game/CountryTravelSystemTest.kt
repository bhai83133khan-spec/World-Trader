package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryTravelSystemTest {
    @Test
    fun countryBrowseIsCaseInsensitiveAndIncludesUnownedCities() {
        val cities = listOf(
            CityData("alex", "Alexandria", "Egypt", 1_200, 6_000, 20, owned = true),
            CityData("delta", "Delta", " egypt ", 500, 1_000, 10, owned = false),
            CityData("ven", "Venice", "Italy", 2_400, 12_000, 40, owned = false)
        )

        val egypt = CountryTravelSystem.citiesInCountry(cities, " eGyPt ")

        assertEquals(listOf("alex", "delta"), egypt.map { it.id })
        assertFalse(egypt.last().owned)
    }

    @Test
    fun countryListIncludesNewCityCountriesWithoutDuplicateLabels() {
        val cities = listOf(
            CityData("city1", "New Settlement", "New Kingdom", 300, 500, 10),
            CityData("city2", "Second Settlement", "new kingdom", 300, 500, 10)
        )
        val countries = CountryTravelSystem.knownCountries(cities, listOf("Egypt", "Italy", "Egypt"))

        assertEquals(listOf("Egypt", "Italy", "New Kingdom"), countries)
        assertEquals(1, countries.count { it.equals("Egypt", ignoreCase = true) })
    }

    @Test
    fun blankOrUnknownCountryReturnsNoDestinations() {
        val city = CityData("alex", "Alexandria", "Egypt", 1_200, 6_000, 20)
        assertTrue(CountryTravelSystem.citiesInCountry(listOf(city), "   ").isEmpty())
        assertTrue(CountryTravelSystem.citiesInCountry(listOf(city), "Persia").isEmpty())
    }
}
