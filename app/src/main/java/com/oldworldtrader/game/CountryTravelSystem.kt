package com.oldworldtrader.game

/** Country browsing is navigation state; it must not alter the player's kingdom/faction identity. */
object CountryTravelSystem {
    fun knownCountries(cities: List<CityData>, presetCountries: List<String>): List<String> =
        (presetCountries + cities.map { it.country })
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }

    /** Return every city in a country, including unowned destinations the player may visit. */
    fun citiesInCountry(cities: List<CityData>, country: String): List<CityData> {
        val normalized = country.trim()
        if (normalized.isEmpty()) return emptyList()
        return cities.filter { it.country.trim().equals(normalized, ignoreCase = true) }
    }
}
