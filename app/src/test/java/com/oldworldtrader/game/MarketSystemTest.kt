package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketSystemTest {
    @Test
    fun trendRiskClassifiesLargePriceMovesForTradeWarnings() {
        val good = TradeGood("Rare Gem", 100)
        val low = MarketSystem.quote(good, cityIndex = 0, year = 1, stock = 120)
        assertEquals("Low", low.trendRisk)

        val moderate = MarketSystem.quote(good, cityIndex = 0, year = 2, stock = 120,
            citySupplyFactor = 100, demandFactor = 110,
            previousCitySupplyFactor = 100, previousDemandFactor = 100)
        assertEquals("Moderate", moderate.trendRisk)

        val high = MarketSystem.quote(good, cityIndex = 0, year = 1, stock = 1,
            citySupplyFactor = 50, demandFactor = 200,
            previousCitySupplyFactor = 200, previousDemandFactor = 50)
        assertEquals("High", high.trendRisk)
    }


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
        val quote = MarketQuote(price = Int.MAX_VALUE, trendPercent = 0, trendLabel = "Stable", availableStock = 10)
        assertFalse(MarketSystem.canBuy(quote, quantity = 2, gold = Int.MAX_VALUE))
        assertTrue(MarketSystem.canBuy(quote, quantity = 1, gold = Int.MAX_VALUE))
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

        val stocked = emptyStock.copy(availableStock = 20)
        assertEquals(20, MarketSystem.updatedStockAfterBuy(stocked, -5))
        assertEquals(20, MarketSystem.updatedStockAfterBuy(stocked, 21))
        assertEquals(20, MarketSystem.updatedStockAfterSell(stocked, 0))
        assertEquals(20, MarketSystem.updatedStockAfterSell(stocked, -5))
    }

    @Test
    fun marketPriceAndUpgradeCostSaturateAtIntMaximum() {
        val gem = TradeGood("Rare Gem", Int.MAX_VALUE)
        val building = BuildingSpec("Vault", Int.MAX_VALUE, 0, 0, "Stores treasure")
        assertEquals(Int.MAX_VALUE, EconomyLogic.marketPrice(gem, Int.MAX_VALUE, Int.MAX_VALUE))
        assertEquals(Int.MAX_VALUE, EconomyLogic.upgradeCost(building, Int.MAX_VALUE))
        assertEquals(0, EconomyLogic.upgradeCost(building, -10))
    }
    @Test
    fun invalidSupplyAndDemandFactorsAreClampedAndNegativeStockIsEmpty() {
        val good = TradeGood("Wood", 45)
        val invalid = MarketSystem.quote(
            good = good,
            cityIndex = 0,
            year = 1,
            stock = -9,
            citySupplyFactor = Int.MIN_VALUE,
            demandFactor = Int.MAX_VALUE
        )
        val clamped = MarketSystem.quote(
            good = good,
            cityIndex = 0,
            year = 1,
            stock = -9,
            citySupplyFactor = 50,
            demandFactor = 200
        )
        assertEquals(clamped.price, invalid.price)
        assertEquals(0, invalid.availableStock)
        assertTrue(invalid.price > 0)
        assertTrue(invalid.trendPercent in -99..999)
    }
    @Test
    fun lowStockRaisesPriceComparedWithAbundantStock() {
        val good = TradeGood("Wheat", 30)
        val scarce = MarketSystem.quote(good, cityIndex = 0, year = 1, stock = 5)
        val abundant = MarketSystem.quote(good, cityIndex = 0, year = 1, stock = 300)
        assertTrue("Scarcity should raise price", scarce.price > abundant.price)
    }

    @Test
    fun higherDemandRaisesPriceAndHigherSupplyLowersPrice() {
        val good = TradeGood("Wheat", 30)
        val baseline = MarketSystem.quote(good, cityIndex = 0, year = 1)
        val highDemand = MarketSystem.quote(good, cityIndex = 0, year = 1, demandFactor = 150)
        val highSupply = MarketSystem.quote(good, cityIndex = 0, year = 1, citySupplyFactor = 150)
        assertTrue(highDemand.price > baseline.price)
        assertTrue(highSupply.price < baseline.price)
    }

    @Test
    fun priceFactorsAreClampedToSupportedRange() {
        val good = TradeGood("Wheat", 30)
        val low = MarketSystem.quote(good, cityIndex = 0, year = 1, citySupplyFactor = 1, demandFactor = 1)
        val clampedLow = MarketSystem.quote(good, cityIndex = 0, year = 1, citySupplyFactor = 50, demandFactor = 50)
        val high = MarketSystem.quote(good, cityIndex = 0, year = 1, citySupplyFactor = 999, demandFactor = 999)
        val clampedHigh = MarketSystem.quote(good, cityIndex = 0, year = 1, citySupplyFactor = 200, demandFactor = 200)
        assertEquals(clampedLow.price, low.price)
        assertEquals(clampedHigh.price, high.price)
    }
    @Test
    fun seasonalMarketEventsAreDeterministicAndBounded() {
        val wheat = TradeGood("Wheat", 30)
        val first = MarketSystem.factorsFor(wheat, cityIndex = 0, year = 2)
        val repeated = MarketSystem.factorsFor(wheat, cityIndex = 0, year = 2)
        assertEquals(first, repeated)
        assertTrue(first.supplyFactor in 50..200)
        assertTrue(first.demandFactor in 50..200)
        assertEquals("High demand", first.label)
    }

    @Test
    fun seasonalQuoteTrendComparesCurrentAndPreviousYearFactors() {
        val wheat = TradeGood("Wheat", 30)
        val current = MarketSystem.factorsFor(wheat, cityIndex = 0, year = 2)
        val previous = MarketSystem.factorsFor(wheat, cityIndex = 0, year = 1)
        val quote = MarketSystem.quote(
            good = wheat,
            cityIndex = 0,
            year = 2,
            stock = 120,
            citySupplyFactor = current.supplyFactor,
            demandFactor = current.demandFactor,
            previousCitySupplyFactor = previous.supplyFactor,
            previousDemandFactor = previous.demandFactor,
            eventLabel = current.label
        )
        assertEquals("High demand", quote.eventLabel)
        assertTrue("Prior-year shortage should be reflected in the trend", quote.trendPercent < 0)
    }

}
