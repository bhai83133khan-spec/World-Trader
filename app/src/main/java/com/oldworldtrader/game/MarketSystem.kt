package com.oldworldtrader.game

/**
 * City-level market helpers. Prices and stock calculations are bounded to avoid
 * integer overflow from extreme market inputs.
 */
data class MarketQuote(
    val price: Int,
    val trendPercent: Int,
    val trendLabel: String,
    val availableStock: Int
)

object MarketSystem {
    private const val BASE_STOCK = 120
    private const val MAX_INT = Int.MAX_VALUE.toLong()

    fun quote(
        good: TradeGood,
        cityIndex: Int,
        year: Int,
        stock: Int = BASE_STOCK,
        citySupplyFactor: Int = 100,
        demandFactor: Int = 100
    ): MarketQuote {
        val stockFactor = when {
            stock <= 0 -> 175
            stock < 20 -> 145
            stock < 50 -> 120
            stock < 100 -> 105
            stock > 250 -> 85
            else -> 100
        }
        val supply = citySupplyFactor.coerceIn(50, 200)
        val demand = demandFactor.coerceIn(50, 200)
        val price = scaledPrice(EconomyLogic.marketPrice(good, cityIndex, year), stockFactor, demand, supply)
        val previousPrice = scaledPrice(
            EconomyLogic.marketPrice(good, cityIndex, (year - 1).coerceAtLeast(1)),
            stockFactor,
            demand,
            supply
        )
        val trend = (((price.toLong() - previousPrice.toLong()) * 100L) / previousPrice.toLong())
            .coerceIn(-99L, 999L).toInt()
        val label = when {
            trend >= 5 -> "Rising"
            trend <= -5 -> "Falling"
            else -> "Stable"
        }
        return MarketQuote(price, trend, label, stock.coerceAtLeast(0))
    }

    private fun scaledPrice(basePrice: Int, stockFactor: Int, demand: Int, supply: Int): Int {
        val numerator = basePrice.toLong().coerceAtLeast(1L) *
            stockFactor.toLong().coerceAtLeast(1L) *
            demand.toLong().coerceAtLeast(1L)
        val denominator = supply.toLong().coerceAtLeast(1L) * 100L
        return (numerator / denominator).coerceIn(1L, MAX_INT).toInt()
    }

    fun canBuy(quote: MarketQuote, quantity: Int, gold: Int): Boolean {
        if (quantity <= 0 || quote.price <= 0 || gold < 0 || quantity > quote.availableStock) return false
        return quote.price.toLong() * quantity.toLong() <= gold.toLong()
    }

    fun canSell(inventory: Int, quantity: Int): Boolean =
        quantity > 0 && inventory >= 0 && inventory.toLong() >= quantity.toLong()

    fun updatedStockAfterBuy(quote: MarketQuote, quantity: Int): Int {
        val stock = quote.availableStock.coerceAtLeast(0)
        if (quantity <= 0 || quantity > stock) return stock
        return (stock.toLong() - quantity.toLong()).coerceIn(0L, MAX_INT).toInt()
    }


    fun updatedStockAfterSell(quote: MarketQuote, quantity: Int): Int {
        val stock = quote.availableStock.coerceAtLeast(0)
        if (quantity <= 0) return stock
        return (stock.toLong() + quantity.toLong()).coerceIn(0L, MAX_INT).toInt()
    }
}
