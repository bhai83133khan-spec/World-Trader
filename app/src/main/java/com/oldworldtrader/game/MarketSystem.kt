package com.oldworldtrader.game

/**
 * City-level market helpers. These are deterministic for a given city/year/good,
 * so prices are stable until the player advances the game year or inventory changes.
 */
data class MarketQuote(
    val price: Int,
    val trendPercent: Int,
    val trendLabel: String,
    val availableStock: Int
)

object MarketSystem {
    private const val BASE_STOCK = 120

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
        val price = (EconomyLogic.marketPrice(good, cityIndex, year) *
            stockFactor * demand / (supply * 100)).coerceAtLeast(1)
        val previousPrice = (EconomyLogic.marketPrice(good, cityIndex, (year - 1).coerceAtLeast(1)) *
            stockFactor * demand / (supply * 100)).coerceAtLeast(1
        )
        val trend = ((price - previousPrice) * 100 / previousPrice).coerceIn(-99, 999)
        val label = when {
            trend >= 5 -> "Rising"
            trend <= -5 -> "Falling"
            else -> "Stable"
        }
        return MarketQuote(price, trend, label, stock.coerceAtLeast(0))
    }

    fun canBuy(quote: MarketQuote, quantity: Int, gold: Int): Boolean =
        quantity > 0 && quantity <= quote.availableStock && gold >= quote.price * quantity

    fun canSell(inventory: Int, quantity: Int): Boolean = quantity > 0 && inventory >= quantity

    fun updatedStockAfterBuy(quote: MarketQuote, quantity: Int): Int =
        (quote.availableStock - quantity).coerceAtLeast(0)

    fun updatedStockAfterSell(quote: MarketQuote, quantity: Int): Int =
        quote.availableStock + quantity.coerceAtLeast(0)
}
