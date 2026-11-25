package com.oldworldtrader.game

data class KingdomMetrics(
    val ownedCities: Int,
    val ownedPopulation: Long,
    val ownedBuildingLevels: Long
)

data class KingdomRankSpec(
    val title: String,
    val ownedCitiesRequired: Int,
    val populationRequired: Long,
    val buildingLevelsRequired: Long,
    val foundingDiscountPercent: Int
)

data class KingdomSnapshot(
    val metrics: KingdomMetrics,
    val currentRank: KingdomRankSpec,
    val nextRank: KingdomRankSpec?
)

/** Derived kingdom ranks and their practical settlement-founding benefits. */
object KingdomProgression {
    val ranks = listOf(
        KingdomRankSpec("Merchant", 0, 0, 0, 0),
        KingdomRankSpec("Baron", 1, 0, 0, 5),
        KingdomRankSpec("Count", 2, 1_000, 2, 10),
        KingdomRankSpec("Duke", 3, 6_000, 6, 15),
        KingdomRankSpec("Kingdom", 4, 12_000, 10, 20),
        KingdomRankSpec("Empire", 5, 25_000, 18, 25)
    )

    fun metrics(cities: List<CityData>): KingdomMetrics {
        val owned = cities.filter { it.owned }
        val population = owned.sumOf { it.population.coerceAtLeast(0).toLong() }
        val buildingLevels = owned.sumOf { city ->
            city.buildings.values.sumOf { level -> level.coerceAtLeast(0).toLong() }
        }
        return KingdomMetrics(owned.size, population, buildingLevels)
    }

    fun evaluate(cities: List<CityData>): KingdomSnapshot {
        val metrics = metrics(cities)
        val currentIndex = ranks.indexOfLast { qualifies(it, metrics) }.coerceAtLeast(0)
        return KingdomSnapshot(
            metrics = metrics,
            currentRank = ranks[currentIndex],
            nextRank = ranks.getOrNull(currentIndex + 1)
        )
    }

    /** Apply the current rank's discount to the base settlement cost using safe Long arithmetic. */
    fun foundingCost(cities: List<CityData>, baseCost: Int = 2_500): Int {
        val safeBase = baseCost.coerceAtLeast(0)
        val discount = evaluate(cities).currentRank.foundingDiscountPercent.coerceIn(0, 100)
        return (safeBase.toLong() * (100L - discount.toLong()) / 100L)
            .coerceIn(0L, Int.MAX_VALUE.toLong())
            .toInt()
    }

    private fun qualifies(rank: KingdomRankSpec, metrics: KingdomMetrics): Boolean =
        metrics.ownedCities >= rank.ownedCitiesRequired &&
            metrics.ownedPopulation >= rank.populationRequired &&
            metrics.ownedBuildingLevels >= rank.buildingLevelsRequired
}
