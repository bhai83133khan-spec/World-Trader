package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketInventoryTest {
    private val wheat = TradeGood("Wheat", 30)
    private val quote = MarketQuote(price = 10, trendPercent = 0, trendLabel = "Stable", availableStock = 5)

    @Test
    fun buyRejectsInvalidPriceAndCargoOverflow() {
        val invalidPrice = MarketState().buy("alex", wheat, quote.copy(price = -1), 1, 100)
        assertFalse(invalidPrice.success)

        val nearLimit = MarketState(cargo = mapOf("Wheat" to Int.MAX_VALUE - 1))
        val overflow = nearLimit.buy("alex", wheat, quote, 2, 100)
        assertFalse(overflow.success)
        assertEquals(Int.MAX_VALUE - 1, overflow.state.cargoAmount("Wheat"))
    }

    @Test
    fun validBuyChangesWalletCargoAndCityStock() {
        val transaction = MarketState().buy("alex", wheat, quote, 2, 100)
        assertTrue(transaction.success)
        assertEquals(80, transaction.gold)
        assertEquals(2, transaction.state.cargoAmount("Wheat"))
        assertEquals(3, transaction.state.stock("alex", "Wheat"))
    }

    @Test
    fun cityStockIsIndependentAndCanBeQuotedSeparately() {
        val initial = MarketState()
        val alexQuote = MarketSystem.quote(wheat, cityIndex = 0, year = 1, stock = initial.stock("alex", "Wheat"))
        val purchase = initial.buy("alex", wheat, alexQuote, quantity = 1, gold = 100)

        assertTrue(purchase.success)
        val alexStock = purchase.state.stock("alex", "Wheat")
        val veniceStock = purchase.state.stock("ven", "Wheat")
        assertEquals(119, alexStock)
        assertEquals(120, veniceStock)

        val updatedAlexQuote = MarketSystem.quote(wheat, cityIndex = 0, year = 1, stock = alexStock)
        val veniceQuote = MarketSystem.quote(wheat, cityIndex = 1, year = 1, stock = veniceStock)
        assertEquals(119, updatedAlexQuote.availableStock)
        assertEquals(120, veniceQuote.availableStock)
        assertTrue(veniceQuote.price > updatedAlexQuote.price)
    }

    @Test
    fun sellRejectsInvalidPriceAndCapsMarketStock() {
        val state = MarketState(
            cityStocks = mapOf("alex" to mapOf("Wheat" to Int.MAX_VALUE - 1)),
            cargo = mapOf("Wheat" to 3)
        )
        assertFalse(state.sell("alex", wheat, quote.copy(price = 0), 1, 0).success)
        val sale = state.sell("alex", wheat, quote, 3, 0)
        assertTrue(sale.success)
        assertEquals(Int.MAX_VALUE, sale.state.stock("alex", "Wheat"))
        assertEquals(30, sale.gold)
        assertEquals(0, sale.state.cargoAmount("Wheat"))
    }
    @Test
    fun perGoodYearlyRestockUsesOverridesAndDefaultRate() {
        val state = MarketState(
            cityStocks = mapOf("alex" to mapOf("Wheat" to 10, "Spice" to 10, "Silk" to 119))
        )
        val nextYear = state.restockYearly(
            defaultRefill = 5,
            refillAmounts = mapOf("Wheat" to 30, "Spice" to 2),
            stockCap = 120
        )
        assertEquals(40, nextYear.stock("alex", "Wheat"))
        assertEquals(12, nextYear.stock("alex", "Spice"))
        assertEquals(120, nextYear.stock("alex", "Silk"))
        assertEquals(10, state.stock("alex", "Wheat"))
    }

    @Test
    fun yearlyRestockRefillsDepletedPortsButPreservesSurplusAndCargo() {
        val state = MarketState(
            cityStocks = mapOf(
                "alex" to mapOf("Wheat" to 10, "Spice" to 150),
                "ven" to mapOf("Wheat" to 110)
            ),
            cargo = mapOf("Wheat" to 4)
        )
        val nextYear = state.restockYearly(refillAmount = 25, stockCap = 120)
        assertEquals(35, nextYear.stock("alex", "Wheat"))
        assertEquals(150, nextYear.stock("alex", "Spice"))
        assertEquals(120, nextYear.stock("ven", "Wheat"))
        assertEquals(4, nextYear.cargoAmount("Wheat"))
        assertEquals(10, state.stock("alex", "Wheat")) // original state remains immutable
    }





}
