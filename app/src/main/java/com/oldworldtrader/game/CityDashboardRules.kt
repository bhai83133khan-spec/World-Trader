package com.oldworldtrader.game

/** Read-only city summary values derived from a city's current state. */
object CityDashboardRules {
    fun annualIncome(city: CityData): Int = if (city.owned) (city.population.toLong() / 20L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt() else 0

    fun annualExpense(city: CityData): Int {
        if (!city.owned) return 0
        val buildingUpkeep = city.buildings.entries.sumOf { (name, level) ->
            val spec = EconomyLogic.buildingSpecs.firstOrNull { it.name == name }
            val upkeepPerLevel = when (name) {
                "Hospital" -> 12
                "Police Station" -> 10
                "Army Barracks" -> 18
                "Power Plant" -> 25
                else -> 4
            }
            level.toLong().coerceAtLeast(0L) * upkeepPerLevel.toLong()
        }
        val populationMaintenance = city.population.toLong() / 100L * 2L
        return (buildingUpkeep + populationMaintenance).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    fun annualNet(city: CityData): Int {
        val net = annualIncome(city).toLong() - annualExpense(city).toLong()
        return net.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
    }

    fun welfareLabel(city: CityData): String = when {
        city.buildings["Hospital"] != null && city.buildings["Park"] != null -> "Excellent"
        city.buildings["Hospital"] != null || city.buildings["Park"] != null -> "Good"
        else -> "Basic"
    }

    fun electricityLabel(city: CityData): String =
        if (CountrySystems.isElectricityUnlocked(city) && city.buildings["Power Plant"] != null) "Powered" else "Not available"
}
