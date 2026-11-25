package com.oldworldtrader.game

/** Rules for country ownership and a once-per-year offline kingdom simulation. */
object WorldTurnSystem {
    const val PLAYER_FACTION = EnemyKingdomSystem.PLAYER_ID

    /** Migration: legacy saves have `owned` but no faction identifier. */
    fun migrateOwnership(cities: List<CityData>): List<CityData> = cities.map { city ->
        if (city.owned && city.controlledBy == null) city.copy(controlledBy = PLAYER_FACTION) else city
    }

    fun enemyTurn(
        state: EnemyKingdomState,
        cities: List<CityData>,
        year: Int,
        playerKingdom: String
    ): EnemyTurnResult {
        val migrated = migrateOwnership(cities)
        return EnemyKingdomSystem.advanceYear(state, migrated, year, playerKingdom)
    }

    fun ownedCityIncome(city: CityData, taxRatePercent: Int = 10): Int {
        if (!city.owned || city.controlledBy != PLAYER_FACTION) return 0
        val populationIncome = city.population.toLong().coerceAtLeast(0L) / 20L
        val buildingIncome = city.buildings.entries.sumOf { (name, level) ->
            val perLevel = when (name) {
                "Market" -> 20L
                "Harbor" -> 35L
                "Workshop" -> 25L
                else -> 0L
            }
            perLevel * level.toLong().coerceAtLeast(0L)
        }
        val taxPercent = taxRatePercent.coerceIn(0, 40).toLong()
        return ((populationIncome + buildingIncome) * taxPercent / 10L)
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    fun kingdomIncome(cities: List<CityData>, taxRatePercent: Int = 10): Int =
        cities.sumOf { ownedCityIncome(it, taxRatePercent).toLong() }
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}
