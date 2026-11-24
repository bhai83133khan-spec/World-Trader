package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MarketInventoryEdgeTest {
    private val wheat = TradeGood("Wheat", 30)
    private val quote = MarketQuote(price = 10, trendPercent = 0, trendLabel = "Stable", availableStock = 120)

    @Test
    fun saleCannotOverflowWalletAndLeavesStateUnchanged() {
        val initial = MarketState(cargo = mapOf("Wheat" to 2))
        val sale = initial.sell("alex", wheat, quote.copy(price = Int.MAX_VALUE), quantity = 2, gold = 1)
        assertFalse(sale.success)
        assertEquals(1, sale.gold)
        assertEquals(2, sale.state.cargoAmount("Wheat"))
        assertEquals(120, sale.state.stock("alex", "Wheat"))
    }

    @Test
    fun zeroAndNegativeQuantitiesNeverMutateMarketOrCargo() {
        val initial = MarketState(cargo = mapOf("Wheat" to 3))
        val zeroBuy = initial.buy("alex", wheat, quote, quantity = 0, gold = 100)
        val zeroSell = initial.sell("alex", wheat, quote, quantity = 0, gold = 100)
        val negativeSell = initial.sell("alex", wheat, quote, quantity = -1, gold = 100)
        assertFalse(zeroBuy.success)
        assertFalse(zeroSell.success)
        assertFalse(negativeSell.success)
        assertEquals(3, initial.cargoAmount("Wheat"))
        assertEquals(120, initial.stock("alex", "Wheat"))
    }

    @Test
    fun corruptedNegativeSavedStockAndCargoAreSanitizedBeforeTransactions() {
        val corrupted = MarketState(
            cityStocks = mapOf("alex" to mapOf("Wheat" to -40)),
            cargo = mapOf("Wheat" to -7)
        )
        assertEquals(0, corrupted.stock("alex", "Wheat"))
        assertEquals(0, corrupted.cargoAmount("Wheat"))

        val zeroStockQuote = quote.copy(availableStock = 0)
        val buy = corrupted.buy("alex", wheat, zeroStockQuote, quantity = 1, gold = 100)
        val sell = corrupted.sell("alex", wheat, zeroStockQuote, quantity = 1, gold = 100)
        assertFalse(buy.success)
        assertFalse(sell.success)
        assertEquals(0, buy.state.stock("alex", "Wheat"))
        assertEquals(0, buy.state.cargoAmount("Wheat"))
        assertEquals(0, sell.state.stock("alex", "Wheat"))
        assertEquals(0, sell.state.cargoAmount("Wheat"))
    }
    @Test
    fun invalidPriceAndNegativeWalletNeverMutateMarketState() {
        val initial = MarketState(
            cityStocks = mapOf("alex" to mapOf("Wheat" to 5)),
            cargo = mapOf("Wheat" to 2)
        )
        val badBuyPrice = initial.buy("alex", wheat, quote.copy(price = 0), quantity = 1, gold = 100)
        val negativeWalletBuy = initial.buy("alex", wheat, quote, quantity = 1, gold = -1)
        val badSellPrice = initial.sell("alex", wheat, quote.copy(price = -10), quantity = 1, gold = 100)
        val negativeWalletSell = initial.sell("alex", wheat, quote, quantity = 1, gold = -1)

        assertFalse(badBuyPrice.success)
        assertFalse(negativeWalletBuy.success)
        assertFalse(badSellPrice.success)
        assertFalse(negativeWalletSell.success)
        listOf(badBuyPrice, negativeWalletBuy, badSellPrice, negativeWalletSell).forEach {
            assertEquals(initial, it.state)
        }
        assertEquals(100, badBuyPrice.gold)
        assertEquals(-1, negativeWalletBuy.gold)
        assertEquals(100, badSellPrice.gold)
        assertEquals(-1, negativeWalletSell.gold)
    }

}
