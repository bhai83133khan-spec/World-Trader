package com.oldworldtrader.game

/** City-specific market stock and player cargo transaction engine. */
data class MarketState(
    val cityStocks: Map<String, Map<String, Int>> = emptyMap(),
    val cargo: Map<String, Int> = emptyMap()
) {
    fun stock(cityId: String, goodName: String, defaultStock: Int = 120): Int =
        cityStocks[cityId]?.get(goodName) ?: defaultStock

    fun cargoAmount(goodName: String): Int = cargo[goodName] ?: 0

    fun buy(cityId: String, good: TradeGood, quote: MarketQuote, quantity: Int, gold: Int): MarketTransaction {
        if (quantity <= 0) return MarketTransaction(this, gold, false, "Quantity must be at least 1.")
        val available = stock(cityId, good.name, quote.availableStock)
        if (quantity > available) return MarketTransaction(this, gold, false, "Not enough market stock.")
        val cost = quote.price.toLong() * quantity.toLong()
        if (cost > gold.toLong()) return MarketTransaction(this, gold, false, "Not enough gold.")
        val nextStocks = cityStocks.toMutableMap()
        val cityMap = (nextStocks[cityId] ?: emptyMap()).toMutableMap()
        cityMap[good.name] = available - quantity
        nextStocks[cityId] = cityMap
        val nextCargo = cargo.toMutableMap()
        nextCargo[good.name] = cargoAmount(good.name) + quantity
        return MarketTransaction(copy(cityStocks = nextStocks, cargo = nextCargo), gold - cost.toInt(), true, "Bought ${quantity} ${good.name}.")
    }

    fun sell(cityId: String, good: TradeGood, quote: MarketQuote, quantity: Int, gold: Int): MarketTransaction {
        if (quantity <= 0) return MarketTransaction(this, gold, false, "Quantity must be at least 1.")
        val held = cargoAmount(good.name)
        if (quantity > held) return MarketTransaction(this, gold, false, "Not enough cargo to sell.")
        val proceeds = quote.price.toLong() * quantity.toLong()
        if (proceeds > Int.MAX_VALUE || gold.toLong() + proceeds > Int.MAX_VALUE)
            return MarketTransaction(this, gold, false, "Sale value exceeds wallet limit.")
        val nextCargo = cargo.toMutableMap()
        nextCargo[good.name] = held - quantity
        val nextStocks = cityStocks.toMutableMap()
        val cityMap = (nextStocks[cityId] ?: emptyMap()).toMutableMap()
        cityMap[good.name] = stock(cityId, good.name, quote.availableStock) + quantity
        nextStocks[cityId] = cityMap
        return MarketTransaction(copy(cityStocks = nextStocks, cargo = nextCargo), gold + proceeds.toInt(), true, "Sold ${quantity} ${good.name}.")
    }
}

data class MarketTransaction(
    val state: MarketState,
    val gold: Int,
    val success: Boolean,
    val message: String
)
