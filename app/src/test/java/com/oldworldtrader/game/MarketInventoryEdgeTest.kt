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
}
