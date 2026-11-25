package com.oldworldtrader.game

/** Complete, deterministic annual simulation transaction. Invoke exactly once when the player advances a year. */
data class GameYearResult(
    val year: Int,
    val cities: List<CityData>,
    val marketState: MarketState,
    val enemyKingdomState: EnemyKingdomState,
    val battleReports: List<BattleReport>,
    val army: Int,
    val gold: Int,
    val cityIncome: Int,
    val armyUpkeep: Int,
    val message: String
)

object GameTurnSystem {
    fun advanceYear(
        currentYear: Int,
        cities: List<CityData>,
        marketState: MarketState,
        enemyKingdomState: EnemyKingdomState,
        battleReports: List<BattleReport>,
        army: Int,
        gold: Int,
        taxRatePercent: Int = 10,
        playerKingdom: String
    ): GameYearResult {
        if (currentYear == Int.MAX_VALUE) {
            return GameYearResult(
                currentYear, cities, marketState, enemyKingdomState, battleReports,
                army.coerceAtLeast(0), gold.coerceAtLeast(0), 0,
                ArmySystem.annualUpkeep(army), "Maximum year reached."
            )
        }
        val nextYear = currentYear.coerceAtLeast(1) + 1
        val migrated = WorldTurnSystem.migrateOwnership(cities)
        val grown = migrated.map { city ->
            if (city.owned && city.controlledBy == WorldTurnSystem.PLAYER_FACTION) {
                val capacity = CityGrowthSystem.populationCapacity(city)
                city.copy(population = (city.population.toLong() + CityGrowthSystem.annualGrowth(city).toLong())
                    .coerceAtMost(capacity.toLong()).toInt())
            } else city
        }
        val world = WorldTurnSystem.enemyTurn(enemyKingdomState, grown, nextYear, playerKingdom)
        val income = WorldTurnSystem.kingdomIncome(world.cities, taxRatePercent)
        val settlement = ArmySystem.settleYear(army, gold, income.toLong())
        val reports = (battleReports + world.reports).takeLast(30)
        val message = buildString {
            append("Year $nextYear: kingdom income +$income gold; army upkeep ${settlement.upkeep} gold. ")
            if (world.reports.isEmpty()) append("No enemy attacks this year.")
            else append("${world.reports.size} enemy attack${if (world.reports.size == 1) "" else "s"} resolved.")
            if (settlement.desertions > 0) append(" ${settlement.desertions} soldiers deserted.")
        }
        return GameYearResult(
            year = nextYear,
            cities = world.cities,
            marketState = marketState.restockYearly(),
            enemyKingdomState = world.state,
            battleReports = reports,
            army = settlement.army,
            gold = settlement.gold,
            cityIncome = income,
            armyUpkeep = settlement.upkeep,
            message = message
        )
    }
}

/** Housing capacity grows with actual House levels; legacy populations are never truncated. */
object CityGrowthSystem {
    private const val BASE_CAPACITY = 500L
    private const val CAPACITY_PER_HOUSE_LEVEL = 500L

    fun populationCapacity(city: CityData): Int {
        // Save migration can leave the legacy housingLevel and House building level out of sync.
        // Use the greater value so a stale building entry cannot suppress previously granted housing.
        val buildingHouseLevels = (city.buildings["House"] ?: 0).toLong().coerceAtLeast(0L)
        val legacyHouseLevels = (city.housingLevel.toLong() - 1L).coerceAtLeast(0L)
        val houses = maxOf(buildingHouseLevels, legacyHouseLevels)
        val capacity = BASE_CAPACITY + houses * CAPACITY_PER_HOUSE_LEVEL
        return maxOf(city.population.coerceAtLeast(0).toLong(), capacity)
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    fun annualGrowth(city: CityData): Int {
        if (!city.owned || city.population < 0) return 0
        val available = (populationCapacity(city).toLong() - city.population.toLong()).coerceAtLeast(0L)
        val desired = EconomyLogic.populationGrowth(city).toLong().coerceAtLeast(0L)
        return minOf(available, desired).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }
}
