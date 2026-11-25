package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopSystemTest {
    @Test
    fun shopRequiresOwnedCityFundsAndCannotBeOpenedTwice() {
        val empty = ShopState()
        val unowned = empty.openShop("alex", cityOwned = false, gold = 2_000, cargo = emptyMap())
        assertFalse(unowned.success)
        assertEquals(2_000, unowned.gold)
        assertEquals(empty, unowned.state)

        val shortWallet = empty.openShop("alex", cityOwned = true, gold = 999, cargo = emptyMap())
        assertFalse(shortWallet.success)

        val opened = empty.openShop("alex", cityOwned = true, gold = 1_200, cargo = emptyMap())
        assertTrue(opened.success)
        assertEquals(200, opened.gold)
        assertEquals(CityShop(), opened.state.shop("alex"))

        val duplicate = opened.state.openShop("alex", cityOwned = true, gold = 1_200, cargo = emptyMap())
        assertFalse(duplicate.success)
        assertEquals(opened.state, duplicate.state)
        assertEquals(1_200, duplicate.gold)
    }

    @Test
    fun stockTransferMovesCargoIntoShopAndDoesNotChangeWallet() {
        val opened = ShopState().openShop("alex", true, 1_000, emptyMap()).state
        val cargo = mapOf("Wheat" to 4)
        val stocked = opened.stockFromCargo("alex", "Wheat", 3, cargo, 700)
        assertTrue(stocked.success)
        assertEquals(3, stocked.state.stock("alex", "Wheat"))
        assertEquals(1, stocked.cargo["Wheat"])
        assertEquals(700, stocked.gold)
        assertEquals(4, cargo["Wheat"])
    }

    @Test
    fun stockTransferRejectsMissingShopInvalidQuantityInsufficientCargoAndOverflow() {
        val empty = ShopState()
        assertFalse(empty.stockFromCargo("alex", "Wheat", 1, mapOf("Wheat" to 1), 100).success)
        val opened = empty.openShop("alex", true, 1_000, emptyMap()).state
        assertFalse(opened.stockFromCargo("alex", "Wheat", 0, mapOf("Wheat" to 1), 0).success)
        assertFalse(opened.stockFromCargo("alex", "Wheat", 2, mapOf("Wheat" to 1), 0).success)

        val fullShop = ShopState(shops = mapOf("alex" to CityShop(inventory = mapOf("Wheat" to Int.MAX_VALUE))))
        val overflow = fullShop.stockFromCargo("alex", "Wheat", 1, mapOf("Wheat" to 1), 0)
        assertFalse(overflow.success)
        assertEquals(Int.MAX_VALUE, overflow.state.stock("alex", "Wheat"))
    }

    @Test
    fun customerSaleReducesInventoryAndAccumulatesShopCashAndLifetimeTotals() {
        val initial = ShopState(shops = mapOf("alex" to CityShop(inventory = mapOf("Wheat" to 3))))
        val sale = initial.sellToCustomers("alex", "Wheat", 2, retailPrice = 38, gold = 500, cargo = emptyMap())
        assertTrue(sale.success)
        val shop = sale.state.shop("alex")!!
        assertEquals(1, shop.inventory["Wheat"])
        assertEquals(76, shop.cash)
        assertEquals(76, shop.lifetimeRevenue)
        assertEquals(2, shop.itemsSold)
        assertEquals(500, sale.gold)
    }

    @Test
    fun customerSaleRejectsMissingStockAndCashOverflowWithoutMutatingState() {
        val noShop = ShopState()
        assertFalse(noShop.sellToCustomers("alex", "Wheat", 1, 38, 100, emptyMap()).success)
        val stocked = ShopState(shops = mapOf("alex" to CityShop(inventory = mapOf("Wheat" to 1))))
        assertFalse(stocked.sellToCustomers("alex", "Wheat", 0, 38, 100, emptyMap()).success)
        assertFalse(stocked.sellToCustomers("alex", "Wheat", 2, 38, 100, emptyMap()).success)

        val nearlyFull = ShopState(shops = mapOf("alex" to CityShop(inventory = mapOf("Wheat" to 1), cash = Int.MAX_VALUE - 5)))
        val overflow = nearlyFull.sellToCustomers("alex", "Wheat", 1, 10, 100, emptyMap())
        assertFalse(overflow.success)
        assertEquals(nearlyFull, overflow.state)
        assertEquals(1, overflow.state.stock("alex", "Wheat"))
    }

    @Test
    fun collectingShopCashTransfersToWalletAndClearsCashOnly() {
        val initial = ShopState(shops = mapOf("alex" to CityShop(cash = 250, lifetimeRevenue = 900, itemsSold = 10)))
        val collected = initial.collectCash("alex", gold = 1_200, cargo = emptyMap())
        assertTrue(collected.success)
        assertEquals(1_450, collected.gold)
        assertEquals(0, collected.state.shop("alex")!!.cash)
        assertEquals(900, collected.state.shop("alex")!!.lifetimeRevenue)
        assertEquals(10, collected.state.shop("alex")!!.itemsSold)

        val overflow = initial.collectCash("alex", gold = Int.MAX_VALUE - 100, cargo = emptyMap())
        assertFalse(overflow.success)
        assertEquals(initial, overflow.state)
        assertEquals(Int.MAX_VALUE - 100, overflow.gold)
    }

    @Test
    fun retailMarkupRoundsUpAndSaturates() {
        assertEquals(38, ShopSystem.retailPrice(30))
        assertEquals(2, ShopSystem.retailPrice(1))
        assertEquals(2, ShopSystem.retailPrice(0))
        assertEquals(Int.MAX_VALUE, ShopSystem.retailPrice(Int.MAX_VALUE))
    }
}
