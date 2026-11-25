package com.oldworldtrader.game

data class GarrisonTransferResult(
    val success: Boolean,
    val fieldArmy: Int,
    val garrison: Int,
    val transferred: Int,
    val message: String
)

data class GarrisonRelocationResult(
    val success: Boolean,
    val garrisons: Map<String, Int>,
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

    /** Largest safe withdrawal without overflowing the field army's Int storage. */
    fun maxWithdrawable(fieldArmy: Int, garrison: Int, city: CityData): Int {
        if (fieldArmy < 0 || garrison <= 0 || !isPlayerControlled(city)) return 0
        val roomInFieldArmy = Int.MAX_VALUE.toLong() - fieldArmy.toLong()
        return minOf(garrison.toLong(), roomInFieldArmy.coerceAtLeast(0L))
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    /** Maximum soldiers transferable into this city while respecting both garrisons' validity and capacity. */
    fun maxTransferable(cityGarrison: Int, destinationGarrison: Int, destination: CityData): Int {
        if (cityGarrison <= 0 || destinationGarrison < 0 || !isPlayerControlled(destination)) return 0
        val destinationCapacity = capacity(destination)
        if (destinationGarrison > destinationCapacity) return 0
        return minOf(cityGarrison.toLong(), destinationCapacity.toLong() - destinationGarrison.toLong())
            .coerceAtLeast(0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    /** Move troops between two player-controlled city garrisons as one immutable transaction. */
    fun transfer(
        garrisons: Map<String, Int>,
        cities: List<CityData>,
        fromCityId: String,
        toCityId: String,
        amount: Int
    ): GarrisonRelocationResult {
        if (fromCityId.isBlank() || toCityId.isBlank() || fromCityId == toCityId || amount <= 0) {
            return rejectRelocation(garrisons, "Invalid garrison transfer.")
        }
        val source = cities.firstOrNull { it.id == fromCityId }
            ?: return rejectRelocation(garrisons, "Source city not found.")
        val destination = cities.firstOrNull { it.id == toCityId }
            ?: return rejectRelocation(garrisons, "Destination city not found.")
        if (!isPlayerControlled(source) || !isPlayerControlled(destination)) {
            return rejectRelocation(garrisons, "You must control both cities to transfer troops.")
        }
        val sourceCount = garrisons[fromCityId] ?: 0
        val destinationCount = garrisons[toCityId] ?: 0
        if (sourceCount < 0 || destinationCount < 0 || sourceCount > capacity(source) || destinationCount > capacity(destination)) {
            return rejectRelocation(garrisons, "Garrison state exceeds a city's capacity.")
        }
        if (sourceCount < amount) return rejectRelocation(garrisons, "Only $sourceCount soldiers are stationed in ${source.name}.")
        val availableCapacity = capacity(destination).toLong() - destinationCount.toLong()
        if (amount.toLong() > availableCapacity) {
            return rejectRelocation(garrisons, "${destination.name} can receive only ${availableCapacity.coerceAtLeast(0L)} soldiers.")
        }
        val updated = garrisons.toMutableMap()
        val remaining = sourceCount - amount
        if (remaining == 0) updated.remove(fromCityId) else updated[fromCityId] = remaining
        updated[toCityId] = destinationCount + amount
        return GarrisonRelocationResult(true, updated.toMap(), amount,
            "$amount soldiers transferred from ${source.name} to ${destination.name}.")
    }

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

    private fun rejectRelocation(garrisons: Map<String, Int>, message: String) =
        GarrisonRelocationResult(false, garrisons, 0, message)
}
