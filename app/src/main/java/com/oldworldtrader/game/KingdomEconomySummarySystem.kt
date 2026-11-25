package com.oldworldtrader.game

data class KingdomEconomySummary(
    val ownedCities: Int,
    val ownedPopulation: Long,
    val grossAnnualIncome: Int,
    val cityServiceUpkeep: Int,
    val fieldArmyUpkeep: Int,
    val garrisonUpkeep: Int,
    val netAnnualBalance: Int
)

/** Aggregates the same offline income/upkeep rules used by annual settlement for the Kingdom view. */
object KingdomEconomySummarySystem {
    fun evaluate(
        cities: List<CityData>,
        fieldArmy: Int,
        cityGarrisons: Map<String, Int>,
        taxRatePercent: Int = 10
    ): KingdomEconomySummary {
        val ownedCities = WorldTurnSystem.migrateOwnership(cities).filter(CityGarrisonSystem::isPlayerControlled)
        val grossIncome = WorldTurnSystem.kingdomIncome(ownedCities, taxRatePercent)
        val cityUpkeep = WorldTurnSystem.kingdomCityUpkeep(ownedCities)
        val armyUpkeep = ArmySystem.annualUpkeep(fieldArmy)
        val safeGarrisons = CityGarrisonSystem.sanitize(cityGarrisons, ownedCities)
        val garrisonUpkeep = CityGarrisonSystem.total(safeGarrisons)
        val netBalance = (grossIncome.toLong() - cityUpkeep.toLong() - armyUpkeep.toLong() - garrisonUpkeep.toLong())
            .coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
        val population = ownedCities.sumOf { it.population.toLong().coerceAtLeast(0L) }

        return KingdomEconomySummary(
            ownedCities = ownedCities.size,
            ownedPopulation = population,
            grossAnnualIncome = grossIncome,
            cityServiceUpkeep = cityUpkeep,
            fieldArmyUpkeep = armyUpkeep,
            garrisonUpkeep = garrisonUpkeep,
            netAnnualBalance = netBalance
        )
    }
}
