package com.oldworldtrader.game

 data class GarrisonTransferResult(
    val success: Boolean,
    val fieldArmy: Int,
    val garrison: Int,
    val transferred: Int,
    val message: String
)

/** Offline city garrison transfers, capacity, defense contribution and upkeep. */
object CityGarrisonSystem {
    const val BASE_CAPACITY = 100
    const val SOLDIERS_PER_BARRACKS_LEVEL = 50
    const val MAX_CAPACITY = 10_000

    fun isPlayerControlled(city: CityData): Boolean =
        city.owned && (city.controlledBy == null || city.controlledBy == WorldTurnSystem.PLAYER_FACTION)

    fun capacity(city: CityData): Int {
        if (!isPlayerControlled(city)) return 0
        val levels = (city.buildings["Army Barracks"] ?: 0).coerceAtLeast(0)
        return (BASE_CAPACITY.toLong() + levels.toLong() * SOLDIERS_PER_BARRACKS_LEVEL)
            .coerceAtMost(MAX_CAPACITY.toLong()).toInt()
    }

    /** Largest safe transfer from the field army that fits this city's current capacity. */
    fun maxStationable(fieldArmy: Int, garrison: Int, city: CityData): Int {
        if (fieldArmy <= 0 || garrison < 0 || !isPlayerControlled(city)) return 0
        return minOf(fieldArmy.toLong(), (capacity(city).toLong() - garrison.toLong()).coerceAtLeast(0L))
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    /** Largest safe transfer from this city's garrison back into the field army. */
    fun maxWithdrawable(garrison: Int, city: CityData): Int =
        if (garrison > 0 && isPlayerControlled(city)) garrison else 0

    fun station(fieldArmy: Int, garrison: Int, amount: Int, city: CityData): GarrisonTransferResult {
        if (fieldArmy < 0 || garrison < 0 || amount <= 0) return reject(fieldArmy, garrison, "Invalid garrison transfer.")
        if (!isPlayerControlled(city)) return reject(fieldArmy, garrison, "You must control this city to station troops.")
        val cap = capacity(city)
        if (garrison > cap || garrison.toLong() + amount.toLong() > cap.toLong()) {
            return reject(fieldArmy, garrison, "City garrison capacity is $cap soldiers.")
        }
        if (fieldArmy < amount) return reject(fieldArmy, garrison, "Not enough field soldiers to station $amount.")
        return GarrisonTransferResult(true, fieldArmy - amount, garrison + amount, amount,
            "$amount soldiers stationed in ${city.name}.")
    }

    fun withdraw(fieldArmy: Int, garrison: Int, amount: Int, city: CityData): GarrisonTransferResult {
        if (fieldArmy < 0 || garrison < 0 || amount <= 0) return reject(fieldArmy, garrison, "Invalid garrison transfer.")
        if (!isPlayerControlled(city)) return reject(fieldArmy, garrison, "You must control this city to withdraw troops.")
        if (garrison < amount) return reject(fieldArmy, garrison, "Only $garrison soldiers are stationed here.")
        if (fieldArmy.toLong() + amount.toLong() > Int.MAX_VALUE) return reject(fieldArmy, garrison, "Field army limit reached.")
        return GarrisonTransferResult(true, fieldArmy + amount, garrison - amount, amount,
            "$amount soldiers withdrawn from ${city.name}.")
    }

    /** One gold per stationed soldier per year. */
    fun annualUpkeep(garrison: Int): Int = garrison.coerceAtLeast(0)

    fun sanitize(garrisons: Map<String, Int>, cities: List<CityData>): Map<String, Int> {
        val owned = cities.filter(::isPlayerControlled).associateBy { it.id }
        return garrisons.mapNotNull { (cityId, count) ->
            val city = owned[cityId] ?: return@mapNotNull null
            val safeCount = count.coerceAtLeast(0).coerceAtMost(capacity(city))
            if (safeCount == 0) null else cityId to safeCount
        }.toMap()
    }

    fun total(garrisons: Map<String, Int>): Int =
        garrisons.values.sumOf { it.coerceAtLeast(0).toLong() }
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

    /** Unpaid garrison upkeep causes desertions from the largest garrisons first (stable city-ID tie break). */
    fun applyDesertions(garrisons: Map<String, Int>, desertions: Int): Map<String, Int> {
        var remaining = desertions.coerceAtLeast(0)
        val updated = garrisons.mapValues { it.value.coerceAtLeast(0) }.toMutableMap()
        val order = updated.entries.sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        for ((cityId, count) in order) {
            if (remaining == 0) break
            val lost = count.coerceAtMost(remaining)
            val survivors = count - lost
            if (survivors == 0) updated.remove(cityId) else updated[cityId] = survivors
            remaining -= lost
        }
        return updated.filterValues { it > 0 }
    }

    private fun reject(fieldArmy: Int, garrison: Int, message: String) =
        GarrisonTransferResult(false, fieldArmy, garrison, 0, message)
}
