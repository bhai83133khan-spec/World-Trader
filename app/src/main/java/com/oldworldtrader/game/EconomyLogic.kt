package com.oldworldtrader.game

import java.math.BigInteger

object EconomyLogic {
    val goods = listOf(TradeGood("Wheat",30), TradeGood("Wood",45), TradeGood("Iron",70), TradeGood("Spices",90), TradeGood("Silk",150))
    val buildingSpecs = listOf(
        BuildingSpec("House",150,0,80,"Housing and population"),
        BuildingSpec("Hospital",700,500,40,"Health and city welfare"),
        BuildingSpec("Police Station",650,500,20,"Safety and order"),
        BuildingSpec("Army Barracks",900,700,0,"Recruit and train troops"),
        BuildingSpec("Temple",500,300,30,"Happiness and morale"),
        BuildingSpec("Park",350,200,15,"City appeal"),
        BuildingSpec("Power Plant",1800,1500,0,"Unlock electricity and powered buildings")
    )

    private val intMax = BigInteger.valueOf(Int.MAX_VALUE.toLong())
    private val priceDivisor = BigInteger.valueOf(10_000L)

    fun marketPrice(good: TradeGood, cityIndex: Int, year: Int): Int {
        // BigInteger prevents intermediate multiplication overflow for extreme city/year values.
        val basePrice = BigInteger.valueOf(good.basePrice.toLong().coerceAtLeast(1L))
        val cityFactor = BigInteger.valueOf((100L + cityIndex.toLong() * 17L).coerceAtLeast(1L))
        val yearFactor = BigInteger.valueOf((100L + (year.toLong() - 1L) * 4L).coerceAtLeast(1L))
        val scaled = basePrice.multiply(cityFactor).multiply(yearFactor).divide(priceDivisor)
            .coerceAtLeast(BigInteger.ONE)
        return if (scaled > intMax) Int.MAX_VALUE else scaled.toInt()
    }

    fun upgradeCost(spec: BuildingSpec, level: Int): Int {
        if (level < 0 || spec.baseCost <= 0) return 0
        val baseCost = BigInteger.valueOf(spec.baseCost.toLong())
        val multiplier = BigInteger.valueOf(level.toLong() + 1L)
        val cost = baseCost.multiply(multiplier)
        return if (cost > intMax) Int.MAX_VALUE else cost.toInt()
    }

    /** Apply one building upgrade without giving unrelated buildings housing/population bonuses. */
    fun applyUpgrade(city: CityData, spec: BuildingSpec): CityData {
        val currentLevel = city.buildings[spec.name] ?: 0
        val nextLevel = (currentLevel.toLong() + 1L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val nextBuildings = city.buildings.toMutableMap().apply { put(spec.name, nextLevel) }
        val nextUnlocked = city.unlocked.toMutableSet().apply {
            if (spec.name == "Power Plant") add("Electricity")
        }
        val nextPopulation = (city.population.toLong() + spec.populationBoost.toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val nextHousing = if (spec.name == "House") {
            (city.housingLevel.toLong() + 1L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        } else city.housingLevel
        return city.copy(
            population = nextPopulation,
            housingLevel = nextHousing,
            buildings = nextBuildings,
            unlocked = nextUnlocked
        )
    }

    fun populationGrowth(city: CityData): Int = if(city.owned) (city.population * 0.03).toInt().coerceAtLeast(10) else 0
    fun canUnlockElectricity(city: CityData): Boolean = city.population >= 1500 || city.buildings["Army Barracks"] != null
}
