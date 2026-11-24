package com.oldworldtrader.game

/** City-specific market stock and player cargo transaction engine. */
data class MarketState(
    val cityStocks: Map<String, Map<String, Int>> = emptyMap(),
    val cargo: Map<String, Int> = emptyMap()
) {
    fun stock(cityId: String, goodName: String, defaultStock: Int = 120): Int =
        (cityStocks[cityId]?.get(goodName) ?: defaultStock).coerceAtLeast(0)

    fun cargoAmount(goodName: String): Int =
        (cargo[goodName] ?: 0).coerceAtLeast(0)

    /** Refill depleted port stock once per in-game year without reducing surplus stock. */
    fun restockYearly(refillAmount: Int = 25, stockCap: Int = 120): MarketState {
        if (refillAmount <= 0 || stockCap < 0 || cityStocks.isEmpty()) return this
        val restocked = cityStocks.mapValues { (_, goods) ->
            goods.mapValues { (_, currentStock) ->
                val stock = currentStock.coerceAtLeast(0)
                if (stock >= stockCap) stock
                else (stock.toLong() + refillAmount.toLong()).coerceAtMost(stockCap.toLong()).toInt()
            }
        }
        return copy(cityStocks = restocked)
    }

    /** Refill depleted port stock once per in-game year without reducing surplus stock. */
    fun restockYearly(refillAmount: Int = 25, stockCap: Int = 120): MarketState {
        if (refillAmount <= 0 || stockCap < 0 || cityStocks.isEmpty()) return this
        val restocked = cityStocks.mapValues { (_, goods) ->
            goods.mapValues { (_, currentStock) ->
                val stock = currentStock.coerceAtLeast(0)
                if (stock >= stockCap) stock
                else (stock.toLong() + refillAmount.toLong()).coerceAtMost(stockCap.toLong()).toInt()
            }
        }
        return copy(cityStocks = restocked)
    }

    fun buy(cityId: String, good: TradeGood, quote: MarketQuote, quantity: Int, gold: Int): MarketTransaction {
        if (quantity <= 0) return MarketTransaction(this, gold, false, "Quantity must be at least 1.")
        if (quote.price <= 0 || gold < 0) return MarketTransaction(this, gold, false, "Invalid market price or wallet.")
        val available = stock(cityId, good.name, quote.availableStock)
        if (quantity > available) return MarketTransaction(this, gold, false, "Not enough market stock.")
        val cost = quote.price.toLong() * quantity.toLong()
        if (cost > gold.toLong()) return MarketTransaction(this, gold, false, "Not enough gold.")
        val currentCargo = cargoAmount(good.name)
        val nextCargoAmount = currentCargo.toLong() + quantity.toLong()
        if (nextCargoAmount > Int.MAX_VALUE) return MarketTransaction(this, gold, false, "Cargo limit exceeded.")
        val nextStocks = cityStocks.toMutableMap()
        val cityMap = (nextStocks[cityId] ?: emptyMap()).toMutableMap()
        cityMap[good.name] = available - quantity
        nextStocks[cityId] = cityMap
        val nextCargo = cargo.toMutableMap()
        nextCargo[good.name] = nextCargoAmount.toInt()
        return MarketTransaction(copy(cityStocks = nextStocks, cargo = nextCargo), gold - cost.toInt(), true, "Bought " + quantity + " " + good.name + ".")
    }

    fun sell(cityId: String, good: TradeGood, quote: MarketQuote, quantity: Int, gold: Int): MarketTransaction {
        if (quantity <= 0) return MarketTransaction(this, gold, false, "Quantity must be at least 1.")
        if (quote.price <= 0 || gold < 0) return MarketTransaction(this, gold, false, "Invalid market price or wallet.")
        val held = cargoAmount(good.name)
        if (quantity > held) return MarketTransaction(this, gold, false, "Not enough cargo to sell.")
        val proceeds = quote.price.toLong() * quantity.toLong()
        if (proceeds > Int.MAX_VALUE || gold.toLong() + proceeds > Int.MAX_VALUE)
            return MarketTransaction(this, gold, false, "Sale value exceeds wallet limit.")
        val nextCargo = cargo.toMutableMap()
        nextCargo[good.name] = held - quantity
        val nextStocks = cityStocks.toMutableMap()
        val cityMap = (nextStocks[cityId] ?: emptyMap()).toMutableMap()
        val newStock = stock(cityId, good.name, quote.availableStock).toLong() + quantity.toLong()
        cityMap[good.name] = newStock.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        nextStocks[cityId] = cityMap
        return MarketTransaction(copy(cityStocks = nextStocks, cargo = nextCargo), gold + proceeds.toInt(), true, "Sold " + quantity + " " + good.name + ".")
    }
}

data class MarketTransaction(
    val state: MarketState,
    val gold: Int,
    val success: Boolean,
    val message: String
)
