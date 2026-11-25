package com.oldworldtrader.game

data class CityTravelResult(
    val success: Boolean,
    val cityId: String?,
    val message: String
)

/** Visiting a city is separate from ownership and the fleet sea-voyage location. */
object CityTravelSystem {
    fun visit(destination: CityData, fleetAtSea: Boolean): CityTravelResult {
        if (fleetAtSea) {
            return CityTravelResult(false, null, "Complete your sea voyage before visiting another city.")
        }
        if (destination.id.isBlank()) {
            return CityTravelResult(false, null, "Invalid city destination.")
        }
        return CityTravelResult(
            success = true,
            cityId = destination.id,
            message = "Visited ${destination.name}. Its market is accessible without owning the city; your fleet stays docked."
        )
    }

    /** Restore a saved market visit, falling back to the fleet port or first city for older saves. */
    fun restoreMarketCityId(savedCityId: String?, cities: List<CityData>, fleetPortId: String): String {
        val validIds = cities.map { it.id }.filter { it.isNotBlank() }.toSet()
        return savedCityId?.takeIf { it in validIds }
            ?: fleetPortId.takeIf { it in validIds }
            ?: validIds.firstOrNull()
            ?: "alex"
    }
}
