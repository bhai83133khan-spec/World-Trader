package com.oldworldtrader.game

/** Values shown on the owned-city dashboard, derived from the rules the game currently applies. */
object CityDashboardRules {
    fun annualGrowth(city: CityData): Int = CityGrowthSystem.annualGrowth(city)

    /** The year-advance action collects income after applying that year's population growth. */
    fun nextYearIncome(city: CityData): Int {
        if (!city.owned) return 0
        val populationAfterGrowth = (city.population.toLong() + annualGrowth(city).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
        return (populationAfterGrowth / 20L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    fun welfareLabel(city: CityData): String {
        val hasHospital = (city.buildings["Hospital"] ?: 0) > 0
        val hasPark = (city.buildings["Park"] ?: 0) > 0
        return when {
            hasHospital && hasPark -> "Excellent"
            hasHospital || hasPark -> "Good"
            else -> "Basic"
        }
    }

    fun electricityLabel(city: CityData): String =
        if (CountrySystems.isElectricityUnlocked(city) && (city.buildings["Power Plant"] ?: 0) > 0) {
            "Powered"
        } else {
            "Not available"
        }

    fun upgradeBlockReason(city: CityData, spec: BuildingSpec, gold: Int, upgradeDiscountPercent: Int = 0): String? {
        if (!city.owned) return "You must own this city before building here."
        if (city.population < spec.unlockPopulation) return "Requires ${spec.unlockPopulation} people."
        val level = (city.buildings[spec.name] ?: 0).coerceAtLeast(0)
        if (level == Int.MAX_VALUE) return "Maximum level reached."
        val cost = ResearchSystem.applyDiscount(EconomyLogic.upgradeCost(spec, level), upgradeDiscountPercent)
        if (gold < cost) return "Need $cost gold; you have $gold."
        return null
    }
}
