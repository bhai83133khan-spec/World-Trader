package com.oldworldtrader.game

enum class Technology(val title:String, val cost:Int, val requiredPopulation:Int, val unlock:String) {
    BASIC_COMMERCE("Basic Commerce",0,0,"Market"),
    URBAN_PLANNING("Urban Planning",1200,800,"Housing upgrades"),
    PUBLIC_HEALTH("Public Health",1800,1200,"Hospital"),
    CITY_GUARD("City Guard",1600,900,"Police Station"),
    MILITARY("Military Organization",2400,1500,"Army Barracks"),
    ELECTRIFICATION("Electrification",5000,2500,"Power Plant and Electricity"),
    CULTURE("Culture",900,600,"Temple and Parks")
}
object ProgressionRules {
    fun available(tech:Technology,population:Int,unlocked:Set<String>) = population>=tech.requiredPopulation && tech.unlock !in unlocked
    fun upgradeHouseGrowth(level:Int)=80+(level*20)
    fun annualPopulationGrowth(population:Int, housingLevel:Int, owned:Boolean):Int =
        if(owned) (population*0.03).toInt().coerceAtLeast(10)+(housingLevel*5) else 0
}
