package com.oldworldtrader.game

/** Local, offline city resource inventory and yearly production. */
data class CityResourceInventory(
    val food: Int = 0,
    val wood: Int = 0,
    val iron: Int = 0
)

data class KingdomResourceStock(
    val food: Long = 0L,
    val wood: Long = 0L,
    val iron: Long = 0L
)

object CityResourceSystem {
    const val MAX_STOCK = 10_000
    const val FARM_FOOD_PER_LEVEL = 80
    const val SAWMILL_WOOD_PER_LEVEL = 40
    const val IRON_MINE_IRON_PER_LEVEL = 25

    fun annualProduction(city: CityData): CityResourceInventory {
        if (!CityGarrisonSystem.isPlayerControlled(city)) return CityResourceInventory()
        return CityResourceInventory(
            food = production(city.buildings["Farm"], FARM_FOOD_PER_LEVEL),
            wood = production(city.buildings["Sawmill"], SAWMILL_WOOD_PER_LEVEL),
            iron = production(city.buildings["Iron Mine"], IRON_MINE_IRON_PER_LEVEL)
        )
    }

    fun sanitize(inventory: CityResourceInventory): CityResourceInventory =
        CityResourceInventory(
            food = inventory.food.coerceIn(0, MAX_STOCK),
            wood = inventory.wood.coerceIn(0, MAX_STOCK),
            iron = inventory.iron.coerceIn(0, MAX_STOCK)
        )

    /** Legacy saves without a cityResources object naturally restore to an empty inventory. */
    fun sanitizeAll(inventories: Map<String, CityResourceInventory>): Map<String, CityResourceInventory> =
        inventories.entries.asSequence()
            .filter { it.key.isNotBlank() }
            .map { it.key to sanitize(it.value) }
            .filter { it.second != CityResourceInventory() }
            .toMap()

    fun advanceYear(
        cities: List<CityData>,
        inventories: Map<String, CityResourceInventory>
    ): Map<String, CityResourceInventory> {
        val next = sanitizeAll(inventories).toMutableMap()
        cities.filter(CityGarrisonSystem::isPlayerControlled).forEach { city ->
            if (city.id.isBlank()) return@forEach
            val current = next[city.id] ?: CityResourceInventory()
            val output = annualProduction(city)
            next[city.id] = CityResourceInventory(
                food = addStock(current.food, output.food),
                wood = addStock(current.wood, output.wood),
                iron = addStock(current.iron, output.iron)
            )
        }
        return next.toMap()
    }

    fun kingdomTotals(
        cities: List<CityData>,
        inventories: Map<String, CityResourceInventory>
    ): KingdomResourceStock {
        val playerCityIds = cities.filter(CityGarrisonSystem::isPlayerControlled).map { it.id }.toSet()
        return KingdomResourceStock(
            food = playerCityIds.sumOf { sanitize(inventories[it] ?: CityResourceInventory()).food.toLong() },
            wood = playerCityIds.sumOf { sanitize(inventories[it] ?: CityResourceInventory()).wood.toLong() },
            iron = playerCityIds.sumOf { sanitize(inventories[it] ?: CityResourceInventory()).iron.toLong() }
        )
    }

    private fun production(level: Int?, amountPerLevel: Int): Int =
        (level ?: 0).coerceAtLeast(0).toLong().times(amountPerLevel.toLong())
            .coerceAtMost(MAX_STOCK.toLong()).toInt()

    private fun addStock(current: Int, produced: Int): Int =
        (current.coerceAtLeast(0).toLong() + produced.coerceAtLeast(0).toLong())
            .coerceAtMost(MAX_STOCK.toLong()).toInt()
}
