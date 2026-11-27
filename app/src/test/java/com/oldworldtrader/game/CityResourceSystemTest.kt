package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Test

class CityResourceSystemTest {
    private fun city(
        id: String,
        owned: Boolean = true,
        controller: String? = null,
        buildings: MutableMap<String, Int> = mutableMapOf()
    ) = CityData(id, id, "Country", 1_000, 1_000, 10, owned = owned, buildings = buildings, controlledBy = controller)

    @Test
    fun annualProductionScalesByBuildingLevelsAndRequiresPlayerControl() {
        val productive = city("home", buildings = mutableMapOf("Farm" to 2, "Sawmill" to 3, "Iron Mine" to 3))
        assertEquals(CityResourceInventory(food = 160, wood = 120, iron = 75), CityResourceSystem.annualProduction(productive))
        assertEquals(CityResourceInventory(), CityResourceSystem.annualProduction(city("neutral", owned = false,
            buildings = mutableMapOf("Farm" to 4))))
        assertEquals(CityResourceInventory(), CityResourceSystem.annualProduction(city("rival", controller = "Rival",
            buildings = mutableMapOf("Farm" to 4))))
    }

    @Test
    fun yearlyProductionAddsStocksIndependentlyAndRespectsCaps() {
        val productive = city("home", buildings = mutableMapOf("Farm" to 2, "Sawmill" to 1, "Iron Mine" to 1))
        val existing = mapOf("home" to CityResourceInventory(food = 9_950, wood = 9_990, iron = 10_000))
        val result = CityResourceSystem.advanceYear(listOf(productive), existing)
        assertEquals(CityResourceInventory(food = 10_000, wood = 10_000, iron = 10_000), result["home"])
    }

    @Test
    fun resourceStockIsIsolatedAndKingdomTotalsExcludeRivalAndNeutralCities() {
        val home = city("home")
        val rival = city("rival", controller = "Rival")
        val neutral = city("neutral", owned = false)
        val stocks = mapOf(
            "home" to CityResourceInventory(food = 10, wood = 20, iron = 30),
            "rival" to CityResourceInventory(food = 900, wood = 800, iron = 700),
            "neutral" to CityResourceInventory(food = 500, wood = 400, iron = 300)
        )
        assertEquals(KingdomResourceStock(food = 10, wood = 20, iron = 30),
            CityResourceSystem.kingdomTotals(listOf(home, rival, neutral), stocks))
    }

    @Test
    fun resourceSanitizationClampsValuesAndDropsEmptyOrInvalidKeys() {
        val result = CityResourceSystem.sanitizeAll(mapOf(
            "home" to CityResourceInventory(food = -5, wood = 10_001, iron = 50),
            "empty" to CityResourceInventory(),
            " " to CityResourceInventory(food = 10)
        ))
        assertEquals(mapOf("home" to CityResourceInventory(food = 0, wood = 10_000, iron = 50)), result)
        assertEquals(emptyMap<String, CityResourceInventory>(), CityResourceSystem.sanitizeAll(emptyMap()))
    }
}
