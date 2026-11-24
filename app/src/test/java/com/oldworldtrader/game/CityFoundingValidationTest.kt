package com.oldworldtrader.game

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
    fun rejectsDuplicateNamesCaseInsensitively() {
        assertFalse(EconomyLogic.canFoundCityName(" alexandria ", cities))
    }
}
