package com.oldworldtrader.game

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SavedGameState(
    val gold: Int,
    val year: Int,
    val army: Int,
    val selectedCountry: String,
    val cities: List<CityData>,
    val marketState: MarketState
)

/** Lightweight, offline save snapshot stored in app-private preferences. */
object GameSaveStore {
    private const val PREFS = "old_world_trader_save"
    private const val KEY_SNAPSHOT = "snapshot"

    fun load(context: Context): SavedGameState? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_SNAPSHOT, null) ?: return null
        return try {
            val root = JSONObject(raw)
            val cityArray = root.optJSONArray("cities") ?: JSONArray()
            val cities = (0 until cityArray.length()).mapNotNull { index ->
                val city = cityArray.optJSONObject(index) ?: return@mapNotNull null
                val buildingsJson = city.optJSONObject("buildings") ?: JSONObject()
                val buildings = mutableMapOf<String, Int>()
                buildingsJson.keys().forEach { key -> buildings[key] = buildingsJson.optInt(key, 0) }
                val unlockedJson = city.optJSONArray("unlocked") ?: JSONArray()
                val unlocked = (0 until unlockedJson.length()).mapNotNull { unlockedJson.optString(it).takeIf(String::isNotBlank) }.toMutableSet()
                CityData(
                    id = city.optString("id"),
                    name = city.optString("name"),
                    country = city.optString("country"),
                    population = city.optInt("population", 0).coerceAtLeast(0),
                    treasuryValue = city.optInt("treasuryValue", 0).coerceAtLeast(0),
                    defense = city.optInt("defense", 0).coerceAtLeast(0),
                    owned = city.optBoolean("owned", false),
                    housingLevel = city.optInt("housingLevel", 1).coerceAtLeast(1),
                    buildings = buildings,
                    unlocked = unlocked
                )
            }
            if (cities.isEmpty()) return null

            val stockJson = root.optJSONObject("cityStocks") ?: JSONObject()
            val stocks = mutableMapOf<String, Map<String, Int>>()
            stockJson.keys().forEach { cityId ->
                val goodsJson = stockJson.optJSONObject(cityId) ?: JSONObject()
                val goods = mutableMapOf<String, Int>()
                goodsJson.keys().forEach { good -> goods[good] = goodsJson.optInt(good, 0).coerceAtLeast(0) }
                stocks[cityId] = goods
            }
            val cargoJson = root.optJSONObject("cargo") ?: JSONObject()
            val cargo = mutableMapOf<String, Int>()
            cargoJson.keys().forEach { good -> cargo[good] = cargoJson.optInt(good, 0).coerceAtLeast(0) }

            SavedGameState(
                gold = root.optInt("gold", 12000).coerceAtLeast(0),
                year = root.optInt("year", 1).coerceAtLeast(1),
                army = root.optInt("army", 0).coerceAtLeast(0),
                selectedCountry = root.optString("selectedCountry", "Ottoman Empire").ifBlank { "Ottoman Empire" },
                cities = cities,
                marketState = MarketState(cityStocks = stocks, cargo = cargo)
            )
        } catch (_: Exception) {
            null
        }
    }

    fun save(context: Context, state: SavedGameState) {
        val root = JSONObject()
            .put("gold", state.gold.coerceAtLeast(0))
            .put("year", state.year.coerceAtLeast(1))
            .put("army", state.army.coerceAtLeast(0))
            .put("selectedCountry", state.selectedCountry)
        val cityArray = JSONArray()
        state.cities.forEach { city ->
            val buildings = JSONObject()
            city.buildings.forEach { (name, level) -> buildings.put(name, level.coerceAtLeast(0)) }
            val unlocked = JSONArray()
            city.unlocked.forEach { unlocked.put(it) }
            cityArray.put(
                JSONObject()
                    .put("id", city.id)
                    .put("name", city.name)
                    .put("country", city.country)
                    .put("population", city.population.coerceAtLeast(0))
                    .put("treasuryValue", city.treasuryValue.coerceAtLeast(0))
                    .put("defense", city.defense.coerceAtLeast(0))
                    .put("owned", city.owned)
                    .put("housingLevel", city.housingLevel.coerceAtLeast(1))
                    .put("buildings", buildings)
                    .put("unlocked", unlocked)
            )
        }
        root.put("cities", cityArray)
        val stockJson = JSONObject()
        state.marketState.cityStocks.forEach { (cityId, goods) ->
            val goodsJson = JSONObject()
            goods.forEach { (good, quantity) -> goodsJson.put(good, quantity.coerceAtLeast(0)) }
            stockJson.put(cityId, goodsJson)
        }
        val cargoJson = JSONObject()
        state.marketState.cargo.forEach { (good, quantity) -> cargoJson.put(good, quantity.coerceAtLeast(0)) }
        root.put("cityStocks", stockJson).put("cargo", cargoJson)

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_SNAPSHOT, root.toString()).apply()
    }
}
