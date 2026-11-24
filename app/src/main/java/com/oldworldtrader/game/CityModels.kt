package com.oldworldtrader.game

data class CityData(
    val id: String,
    val name: String,
    val country: String,
    var population: Int,
    var treasuryValue: Int,
    var defense: Int,
    var owned: Boolean = false,
    var housingLevel: Int = 1,
    var buildings: MutableMap<String, Int> = mutableMapOf(),
    var unlocked: MutableSet<String> = mutableSetOf("Market", "Housing")
)
data class TradeGood(val name: String, val basePrice: Int)
data class BuildingSpec(val name: String, val baseCost: Int, val unlockPopulation: Int, val populationBoost: Int, val description: String)
data class CountryData(val name: String, val cities: List<String>, var armyCount: Int)
data class ShipData(val name: String, val purchaseCost: Int, val cargoCapacity: Int)
