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

data class FoodConsumptionPreview(
    val demand: Int,
    val available: Int,
    val consumed: Int,
    val shortage: Int,
    val populationLoss: Int,
    val remainingFood: Int
)

data class FoodYearSettlement(
    val inventories: Map<String, CityResourceInventory>,
    val cities: List<CityData>,
    val consumedFood: Map<String, Int>,
    val foodShortages: Map<String, Int>,
    val populationLosses: Map<String, Int>
)

object CityResourceSystem {
    const val MAX_STOCK = 10_000
    const val FARM_FOOD_PER_LEVEL = 80
    const val SAWMILL_WOOD_PER_LEVEL = 40
    const val IRON_MINE_IRON_PER_LEVEL = 25
    const val FOOD_PER_TEN_PEOPLE_PER_YEAR = 1

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

    /** One food unit feeds ten citizens for one simulated year. */
    fun annualFoodDemand(city: CityData): Int =
        if (CityGarrisonSystem.isPlayerControlled(city)) foodDemand(city.population) else 0

    private fun foodDemand(population: Int): Int {
        val people = population.coerceAtLeast(0).toLong()
        val demand = ((people + 9L) / 10L) * FOOD_PER_TEN_PEOPLE_PER_YEAR.toLong()
        return demand.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    /** Forecasts food after next year's population growth and production, then consumption. */
    fun previewAnnualFood(city: CityData, inventory: CityResourceInventory): FoodConsumptionPreview {
        if (!CityGarrisonSystem.isPlayerControlled(city)) return foodPreview(0, 0, 0)
        val current = sanitize(inventory)
        val available = addStock(current.food, annualProduction(city).food)
        val projectedPopulation = (city.population.toLong() + CityGrowthSystem.annualGrowth(city).toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        return foodPreview(projectedPopulation, foodDemand(projectedPopulation), available)
    }

    /** Consumes food from already-produced yearly stocks; shortages cause up to 10% population loss. */
    fun settleFoodConsumption(
        cities: List<CityData>,
        inventories: Map<String, CityResourceInventory>
    ): FoodYearSettlement {
        val nextStocks = sanitizeAll(inventories).toMutableMap()
        val consumed = mutableMapOf<String, Int>()
        val shortages = mutableMapOf<String, Int>()
        val losses = mutableMapOf<String, Int>()
        val nextCities = cities.map { city ->
            if (!CityGarrisonSystem.isPlayerControlled(city) || city.id.isBlank()) return@map city
            val stock = nextStocks[city.id] ?: CityResourceInventory()
            val preview = foodPreview(city.population, annualFoodDemand(city), stock.food)
            nextStocks[city.id] = stock.copy(food = preview.remainingFood)
            consumed[city.id] = preview.consumed
            if (preview.shortage > 0) shortages[city.id] = preview.shortage
            if (preview.populationLoss > 0) losses[city.id] = preview.populationLoss
            if (preview.populationLoss == 0) city else city.copy(
                population = (city.population.toLong() - preview.populationLoss.toLong())
                    .coerceAtLeast(0L).toInt()
            )
        }
        return FoodYearSettlement(
            inventories = sanitizeAll(nextStocks),
            cities = nextCities,
            consumedFood = consumed.toMap(),
            foodShortages = shortages.toMap(),
            populationLosses = losses.toMap()
        )
    }

    private fun foodPreview(population: Int, demand: Int, available: Int): FoodConsumptionPreview {
        val required = demand.coerceAtLeast(0)
        val stock = available.coerceAtLeast(0)
        val consumed = minOf(required, stock)
        val shortage = required - consumed
        val loss = populationLoss(population, shortage, required)
        return FoodConsumptionPreview(
            demand = required, available = stock, consumed = consumed, shortage = shortage,
            populationLoss = loss, remainingFood = stock - consumed
        )
    }

    private fun populationLoss(population: Int, shortage: Int, demand: Int): Int {
        val people = population.coerceAtLeast(0).toLong()
        if (people == 0L || shortage <= 0 || demand <= 0) return 0
        val denominator = demand.toLong() * 10L
        val numerator = people * shortage.toLong()
        val calculated = (numerator + denominator - 1L) / denominator
        val maxAnnualLoss = (people + 9L) / 10L
        return minOf(calculated, maxAnnualLoss, people).toInt()
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
