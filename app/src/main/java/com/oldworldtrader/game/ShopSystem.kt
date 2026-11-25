package com.oldworldtrader.game

/** Persistent city-owned retail shop state and offline customer transactions. */
data class CityShop(
    val inventory: Map<String, Int> = emptyMap(),
    /** Gold earned by customers but not yet collected by the player. */
    val cash: Int = 0,
    val lifetimeRevenue: Int = 0,
    val itemsSold: Int = 0
)

data class ShopTransaction(
    val state: ShopState,
    val gold: Int,
    val cargo: Map<String, Int>,
    val success: Boolean,
    val message: String
)

data class ShopState(val shops: Map<String, CityShop> = emptyMap()) {
    fun shop(cityId: String): CityShop? = shops[cityId]

    fun stock(cityId: String, goodName: String): Int =
        (shops[cityId]?.inventory?.get(goodName) ?: 0).coerceAtLeast(0)

    fun openShop(cityId: String, cityOwned: Boolean, gold: Int, cargo: Map<String, Int>, cost: Int = ShopSystem.BUILD_COST): ShopTransaction {
        if (cityId.isBlank()) return rejected(gold, cargo, "Select a city first.")
        if (!cityOwned) return rejected(gold, cargo, "You must own this city to open a shop.")
        if (gold < 0 || cost <= 0) return rejected(gold, cargo, "Invalid wallet or shop cost.")
        if (shops.containsKey(cityId)) return rejected(gold, cargo, "You already own a shop in this city.")
        if (gold < cost) return rejected(gold, cargo, "Need $cost gold to open a shop.")
        return ShopTransaction(copy(shops = shops + (cityId to CityShop())), gold - cost, cargo, true, "Shop opened! Stock it with goods from your cargo.")
    }

    /** Transfer goods from player cargo into a city shop without changing the wallet. */
    fun stockFromCargo(cityId: String, goodName: String, quantity: Int, cargo: Map<String, Int>, gold: Int): ShopTransaction {
        val currentShop = shops[cityId] ?: return rejected(gold, cargo, "Open a shop in this city first.")
        if (goodName.isBlank() || quantity <= 0) return rejected(gold, cargo, "Quantity must be at least 1.")
        if (gold < 0) return rejected(gold, cargo, "Invalid wallet.")
        val held = (cargo[goodName] ?: 0).coerceAtLeast(0)
        if (quantity > held) return rejected(gold, cargo, "Not enough $goodName in your cargo.")
        val currentStock = (currentShop.inventory[goodName] ?: 0).coerceAtLeast(0)
        val nextStock = currentStock.toLong() + quantity.toLong()
        if (nextStock > Int.MAX_VALUE) return rejected(gold, cargo, "Shop inventory limit exceeded.")
        val nextInventory = currentShop.inventory.toMutableMap().apply { put(goodName, nextStock.toInt()) }
        val nextShops = shops.toMutableMap().apply { put(cityId, currentShop.copy(inventory = nextInventory)) }
        val nextCargo = cargo.toMutableMap().apply { put(goodName, held - quantity) }
        return ShopTransaction(copy(shops = nextShops), gold, nextCargo, true, "Stocked $quantity $goodName in your shop.")
    }

    /** Customer purchases move shop stock into cash; cash is collected separately. */
    fun sellToCustomers(cityId: String, goodName: String, quantity: Int, retailPrice: Int, gold: Int, cargo: Map<String, Int>): ShopTransaction {
        val currentShop = shops[cityId] ?: return rejected(gold, cargo, "Open a shop in this city first.")
        if (goodName.isBlank() || quantity <= 0) return rejected(gold, cargo, "Quantity must be at least 1.")
        if (retailPrice <= 0 || gold < 0) return rejected(gold, cargo, "Invalid sale price or wallet.")
        val held = (currentShop.inventory[goodName] ?: 0).coerceAtLeast(0)
        if (quantity > held) return rejected(gold, cargo, "Not enough $goodName in shop inventory.")
        val proceeds = retailPrice.toLong() * quantity.toLong()
        if (proceeds > Int.MAX_VALUE || currentShop.cash.toLong().coerceAtLeast(0L) + proceeds > Int.MAX_VALUE.toLong()) {
            return rejected(gold, cargo, "Shop cash limit exceeded.")
        }
        val nextInventory = currentShop.inventory.toMutableMap().apply { put(goodName, held - quantity) }
        val nextShop = currentShop.copy(
            inventory = nextInventory,
            cash = (currentShop.cash.coerceAtLeast(0).toLong() + proceeds).toInt(),
            lifetimeRevenue = (currentShop.lifetimeRevenue.toLong().coerceAtLeast(0L) + proceeds).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            itemsSold = (currentShop.itemsSold.toLong().coerceAtLeast(0L) + quantity.toLong()).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        )
        return ShopTransaction(copy(shops = shops + (cityId to nextShop)), gold, cargo, true, "Customers bought $quantity $goodName for $proceeds gold. Collect your shop cash when ready.")
    }

    fun collectCash(cityId: String, gold: Int, cargo: Map<String, Int>): ShopTransaction {
        val currentShop = shops[cityId] ?: return rejected(gold, cargo, "Open a shop in this city first.")
        val cash = currentShop.cash.coerceAtLeast(0)
        if (gold < 0) return rejected(gold, cargo, "Invalid wallet.")
        if (cash == 0) return rejected(gold, cargo, "Your shop has no cash to collect.")
        if (gold.toLong() + cash.toLong() > Int.MAX_VALUE.toLong()) return rejected(gold, cargo, "Wallet limit reached; collect less gold first.")
        val nextShop = currentShop.copy(cash = 0)
        return ShopTransaction(copy(shops = shops + (cityId to nextShop)), gold + cash, cargo, true, "Collected $cash gold from your shop.")
    }

    private fun rejected(gold: Int, cargo: Map<String, Int>, message: String) =
        ShopTransaction(this, gold, cargo, false, message)
}

object ShopSystem {
    const val BUILD_COST = 1_000

    /** A simple 25% markup, rounded up so low-priced goods still earn a margin. */
    fun retailPrice(wholesalePrice: Int): Int {
        val base = wholesalePrice.coerceAtLeast(1).toLong()
        return ((base * 125L + 99L) / 100L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }
}
