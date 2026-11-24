package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketSystemTest {
    @Test
    fun extremePricesAndFactorsAreCappedInsteadOfWrapping() {
        val quote = MarketSystem.quote(
            good = TradeGood("Rare Gem", Int.MAX_VALUE),
            cityIndex = Int.MAX_VALUE,
            year = Int.MAX_VALUE,
            stock = 1,
            citySupplyFactor = 50,
            demandFactor = 200
        )
        assertEquals(Int.MAX_VALUE, quote.price)
        assertTrue(quote.trendPercent in -99..999)
    }

    @Test
    fun purchaseCostUsesLongArithmetic() {
        val quote = MarketQuote(price = 2_000_000_000, trendPercent = 0, trendLabel = "Stable", availableStock = 10)
        assertFalse(MarketSystem.canBuy(quote, quantity = 2, gold = Int.MAX_VALUE))
        assertTrue(MarketSystem.canBuy(quote, quantity = 1, gold = 2_000_000_000))
    }

    @Test
    fun buyingRequiresPositiveQuantityEnoughStockAndFunds() {
        val quote = MarketQuote(price = 125, trendPercent = 0, trendLabel = "Stable", availableStock = 10)
        assertTrue(MarketSystem.canBuy(quote, quantity = 2, gold = 250))
        assertFalse(MarketSystem.canBuy(quote, quantity = 0, gold = 250))
        assertFalse(MarketSystem.canBuy(quote, quantity = 11, gold = 2_000))
        assertFalse(MarketSystem.canBuy(quote, quantity = 2, gold = 249))
    }

    @Test
    fun stockUpdatesNeverOverflowOrIncreaseFromInvalidQuantity() {
        val emptyStock = MarketQuote(price = 10, trendPercent = 0, trendLabel = "Stable", availableStock = 0)
        val fullStock = emptyStock.copy(availableStock = Int.MAX_VALUE)
        assertEquals(0, MarketSystem.updatedStockAfterBuy(emptyStock, -5))
        assertEquals(Int.MAX_VALUE, MarketSystem.updatedStockAfterSell(fullStock, 50))
        assertEquals(0, MarketSystem.updatedStockAfterBuy(emptyStock, Int.MAX_VALUE))
    }

    @Test
    fun marketPriceAndUpgradeCostSaturateAtIntMaximum() {
        val gem = TradeGood("Rare Gem", Int.MAX_VALUE)
        val building = BuildingSpec("Vault", Int.MAX_VALUE, 0, 0, "Stores treasure")
        assertEquals(Int.MAX_VALUE, EconomyLogic.marketPrice(gem, Int.MAX_VALUE, Int.MAX_VALUE))
        assertEquals(Int.MAX_VALUE, EconomyLogic.upgradeCost(building, Int.MAX_VALUE))
        assertEquals(0, EconomyLogic.upgradeCost(building, -10))
    }
}
