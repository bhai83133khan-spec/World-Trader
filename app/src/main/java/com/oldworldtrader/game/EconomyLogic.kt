package com.oldworldtrader.game

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
    fun marketPrice(good: TradeGood, cityIndex: Int, year: Int): Int =
        (good.basePrice * (100 + cityIndex * 17) * (100 + (year - 1) * 4) / 10000).coerceAtLeast(1)
    fun upgradeCost(spec: BuildingSpec, level: Int): Int = spec.baseCost * (level + 1)
    fun populationGrowth(city: CityData): Int = if(city.owned) (city.population * 0.03).toInt().coerceAtLeast(10) else 0
    fun canUnlockElectricity(city: CityData): Boolean = city.population >= 1500 || city.buildings["Army Barracks"] != null
}
