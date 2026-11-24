package com.oldworldtrader.game

data class TravelDestination(val label: String, val country: String, val fare: Int, val unlockYear: Int = 1)
object CountrySystems {
    val destinations = listOf(
        TravelDestination("Alexandria Port","Egypt",250),
        TravelDestination("Venice Port","Italy",400),
        TravelDestination("Muscat Port","Oman",300),
        TravelDestination("Delhi Trade Hub","India",500),
        TravelDestination("Baghdad","Iraq",350),
        TravelDestination("Persian Gulf","Persia",450,2)
    )
    fun recruitmentCost(soldiers: Int) = (soldiers * 10).coerceAtLeast(100)
    fun cityIncome(population: Int, owned: Boolean) = if (owned) population / 20 else 0
    fun isElectricityUnlocked(city: CityData) = city.unlocked.contains("Electricity")
}
