package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CityFoundingValidationTest {
    private val cities = listOf(CityData("alex", "Alexandria", "Egypt", 1200, 6000, 20, true))

    @Test
    fun trimsNameAndAcceptsThreeToThirtyTwoCharacters() {
        assertTrue(EconomyLogic.canFoundCityName("  Cairo  ", cities))
        assertTrue(EconomyLogic.canFoundCityName("A".repeat(32), cities))
    }

    @Test
    fun rejectsTooShortOrTooLongName() {
        assertFalse(EconomyLogic.canFoundCityName("AB", cities))
        assertFalse(EconomyLogic.canFoundCityName("A".repeat(33), cities))
    }

    @Test
    fun generatedCityIdStartsAtOneWhenLegacyCitiesHaveNoGeneratedIds() {
        assertEquals("city1", EconomyLogic.nextCityId(cities))
    }

    @Test
    fun generatedCityIdAdvancesPastHighestSuffixInsteadOfUsingCityCount() {
        val settlements = listOf(
            CityData("city1", "One", "Egypt", 100, 1, 1),
            CityData("city2", "Two", "Egypt", 100, 1, 1),
            CityData("city3", "Three", "Egypt", 100, 1, 1),
            CityData("city4", "Four", "Egypt", 100, 1, 1),
            CityData("city6", "Six", "Egypt", 100, 1, 1)
        )
        assertEquals("city7", EconomyLogic.nextCityId(settlements))
    }


    @Test
    fun rejectsDuplicateNamesCaseInsensitively() {
        assertFalse(EconomyLogic.canFoundCityName(" alexandria ", cities))
    }
}
