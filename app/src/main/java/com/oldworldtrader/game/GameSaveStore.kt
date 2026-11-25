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
    val marketState: MarketState,
    val shopState: ShopState = ShopState(),
    val fleetState: FleetState = FleetState(),
    val researchState: ResearchState = ResearchState(),
    val marketCityId: String? = null
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

            val shopsJson = root.optJSONObject("shops") ?: JSONObject()
            val shops = mutableMapOf<String, CityShop>()
            shopsJson.keys().forEach { cityId ->
                val shopJson = shopsJson.optJSONObject(cityId) ?: return@forEach
                val inventoryJson = shopJson.optJSONObject("inventory") ?: JSONObject()
                val inventory = mutableMapOf<String, Int>()
                inventoryJson.keys().forEach { good ->
                    inventory[good] = inventoryJson.optInt(good, 0).coerceAtLeast(0)
                }
                shops[cityId] = CityShop(
                    inventory = inventory,
                    cash = shopJson.optInt("cash", 0).coerceAtLeast(0),
                    lifetimeRevenue = shopJson.optInt("lifetimeRevenue", 0).coerceAtLeast(0),
                    itemsSold = shopJson.optInt("itemsSold", 0).coerceAtLeast(0)
                )
            }

            val fleetJson = root.optJSONObject("fleet")
            val ownedShipsArray = fleetJson?.optJSONArray("ownedShipIds")
            val ownedShipIds = ownedShipsArray?.let { array ->
                (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }.toSet()
            }
            val voyageJson = fleetJson?.optJSONObject("voyage")
            val savedVoyage = voyageJson?.let {
                Voyage(
                    routeId = it.optString("routeId"),
                    destinationPortId = it.optString("destinationPortId"),
                    remainingDays = it.optInt("remainingDays", 0)
                )
            }
            val fleetState = FleetSystem.restore(
                ownedShipIds = ownedShipIds,
                selectedShipId = fleetJson?.optString("selectedShipId"),
                currentPortId = fleetJson?.optString("currentPortId"),
                voyage = savedVoyage
            )
            val marketCityId = CityTravelSystem.restoreMarketCityId(
                root.optString("marketCityId").takeIf { it.isNotBlank() },
                cities,
                fleetState.currentPortId
            )

            val researchedArray = root.optJSONArray("researchedTechIds")
            val researchedIds = researchedArray?.let { array ->
                (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }.toSet()
            } ?: emptySet()

            SavedGameState(
                gold = root.optInt("gold", 12000).coerceAtLeast(0),
                year = root.optInt("year", 1).coerceAtLeast(1),
                army = root.optInt("army", 0).coerceAtLeast(0),
                selectedCountry = root.optString("selectedCountry", "Ottoman Empire").ifBlank { "Ottoman Empire" },
                cities = cities,
                marketState = MarketState(cityStocks = stocks, cargo = cargo),
                shopState = ShopState(shops = shops),
                fleetState = fleetState,
                researchState = ResearchSystem.restore(researchedIds),
                marketCityId = marketCityId
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
            .put("marketCityId", CityTravelSystem.restoreMarketCityId(
                state.marketCityId, state.cities, state.fleetState.currentPortId
            ))
        val researchedArray = JSONArray()
        state.researchState.researchedTechIds.sorted().forEach { researchedArray.put(it) }
        root.put("researchedTechIds", researchedArray)
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
        val shopsJson = JSONObject()
        state.shopState.shops.forEach { (cityId, shop) ->
            val inventoryJson = JSONObject()
            shop.inventory.forEach { (good, quantity) -> inventoryJson.put(good, quantity.coerceAtLeast(0)) }
            shopsJson.put(
                cityId,
                JSONObject()
                    .put("inventory", inventoryJson)
                    .put("cash", shop.cash.coerceAtLeast(0))
                    .put("lifetimeRevenue", shop.lifetimeRevenue.coerceAtLeast(0))
                    .put("itemsSold", shop.itemsSold.coerceAtLeast(0))
            )
        }
        root.put("shops", shopsJson)
        val fleetJson = JSONObject()
        val ownedShips = JSONArray()
        state.fleetState.ownedShipIds.sorted().forEach { ownedShips.put(it) }
        fleetJson.put("ownedShipIds", ownedShips)
            .put("selectedShipId", state.fleetState.selectedShipId)
            .put("currentPortId", state.fleetState.currentPortId)
        state.fleetState.voyage?.let { voyage ->
            fleetJson.put(
                "voyage",
                JSONObject()
                    .put("routeId", voyage.routeId)
                    .put("destinationPortId", voyage.destinationPortId)
                    .put("remainingDays", voyage.remainingDays)
            )
        }
        root.put("fleet", fleetJson)

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_SNAPSHOT, root.toString()).apply()
    }
}
