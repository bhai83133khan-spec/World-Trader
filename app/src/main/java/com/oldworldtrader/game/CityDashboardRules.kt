package com.oldworldtrader.game

/** Values shown on the owned-city dashboard, derived from the rules the game currently applies. */
object CityDashboardRules {
    fun annualGrowth(city: CityData): Int =
        if (CityGarrisonSystem.isPlayerControlled(city)) CityGrowthSystem.annualGrowth(city) else 0

    /** Preview the same post-growth city income used by the annual kingdom settlement, including buildings. */
    fun nextYearIncome(city: CityData, taxRatePercent: Int = 10): Int {
        if (!CityGarrisonSystem.isPlayerControlled(city)) return 0
        val populationAfterGrowth = (city.population.toLong() + annualGrowth(city).toLong())
            .coerceAtMost(CityGrowthSystem.populationCapacity(city).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val projectedCity = WorldTurnSystem.migrateOwnership(listOf(city.copy(population = populationAfterGrowth))).single()
        return WorldTurnSystem.ownedCityIncome(projectedCity, taxRatePercent)
    }

    fun nextYearCityUpkeep(city: CityData): Int {
        if (!CityGarrisonSystem.isPlayerControlled(city)) return 0
        val projectedPopulation = (city.population.toLong() + annualGrowth(city).toLong())
            .coerceAtMost(CityGrowthSystem.populationCapacity(city).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val projectedCity = WorldTurnSystem.migrateOwnership(listOf(city.copy(population = projectedPopulation))).single()
        return WorldTurnSystem.cityServiceUpkeep(projectedCity)
    }

    fun nextYearNetIncome(city: CityData, taxRatePercent: Int = 10): Int =
        (nextYearIncome(city, taxRatePercent).toLong() - nextYearCityUpkeep(city).toLong())
            .coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()

    /** Use the same fortification strength as combat, including defensive buildings. */
    fun defenseStrength(city: CityData, garrison: Int = 0): Int =
        (ArmySystem.cityDefense(city).toLong() + garrison.coerceAtLeast(0).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

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
        if (!CityGarrisonSystem.isPlayerControlled(city)) return "You must own this city before building here."
        if (city.population < spec.unlockPopulation) return "Requires ${spec.unlockPopulation} people."
        val level = (city.buildings[spec.name] ?: 0).coerceAtLeast(0)
        if (level == Int.MAX_VALUE) return "Maximum level reached."
        val cost = ResearchSystem.applyDiscount(EconomyLogic.upgradeCost(spec, level), upgradeDiscountPercent)
        if (gold < cost) return "Need $cost gold; you have $gold."
        return null
    }
}
