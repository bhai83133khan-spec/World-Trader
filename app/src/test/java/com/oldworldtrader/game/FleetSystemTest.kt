package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FleetSystemTest {
    @Test
    fun newGameHasStarterSkiffAndThirtyItemCargoCapacity() {
        val fleet = FleetState()
        assertTrue("skiff" in fleet.ownedShipIds)
        assertEquals("skiff", fleet.selectedShipId)
        assertEquals(30, FleetSystem.selectedCapacity(fleet))
    }

    @Test
    fun shipPurchaseDeductsGoldAndAddsOwnershipWithoutOverflow() {
        val result = FleetSystem.purchase(FleetState(), "sloop", 3_500)
        assertTrue(result.success)
        assertEquals(500, result.gold)
        assertTrue("sloop" in result.state.ownedShipIds)
        assertFalse("sloop" == result.state.selectedShipId)
    }

    @Test
    fun purchaseRejectsUnknownDuplicateAndInsufficientFunds() {
        val initial = FleetState()
        assertFalse(FleetSystem.purchase(initial, "ghost", 20_000).success)
        assertFalse(FleetSystem.purchase(initial, "skiff", 20_000).success)
        val short = FleetSystem.purchase(initial, "cog", 6_499)
        assertFalse(short.success)
        assertEquals(6_499, short.gold)
        assertEquals(initial, short.state)
    }

    @Test
    fun selectingOwnedShipChangesCapacityAndCannotSelectUnownedShip() {
        val bought = FleetSystem.purchase(FleetState(), "cog", 10_000).state
        val selected = FleetSystem.selectShip(bought, "cog", 4_000)
        assertTrue(selected.success)
        assertEquals("cog", selected.state.selectedShipId)
        assertEquals(160, FleetSystem.selectedCapacity(selected.state))
        assertFalse(FleetSystem.selectShip(bought, "carrack", 4_000).success)
    }

    @Test
    fun cargoCapacityIncludesAllGoodsAndRejectsInvalidQuantity() {
        val cargo = mapOf("Wheat" to 10, "Wood" to 15, "Iron" to 3)
        assertTrue(FleetSystem.canFitCargo(cargo, "Silk", 2, 30))
        assertFalse(FleetSystem.canFitCargo(cargo, "Silk", 3, 30))
        assertFalse(FleetSystem.canFitCargo(cargo, "Silk", 0, 30))
        assertFalse(FleetSystem.canFitCargo(cargo, "Silk", 1, -1))
    }

    @Test
    fun routesHaveReturnTripsAndDepartureChargesCostAndSetsTravelState() {
        val fleet = FleetState()
        val route = FleetSystem.routesFrom("alex").first { it.toPortId == "ven" }
        assertTrue(FleetSystem.routes.any { it.fromPortId == "ven" && it.toPortId == "alex" })
        val departure = FleetSystem.depart(fleet, route.id, 1_000)
        assertTrue(departure.success)
        assertEquals(550, departure.gold)
        assertTrue(departure.state.isAtSea)
        assertEquals(3, departure.state.voyage?.remainingDays)
        assertEquals("alex", departure.state.currentPortId)
    }

    @Test
    fun cannotDepartWithoutShipOrWhileAlreadyAtSea() {
        val emptyFleet = FleetState(ownedShipIds = emptySet(), selectedShipId = "skiff")
        val route = FleetSystem.routesFrom("alex").first()
        assertFalse(FleetSystem.depart(emptyFleet, route.id, 10_000).success)
        val atSea = FleetSystem.depart(FleetState(), route.id, 10_000).state
        assertFalse(FleetSystem.depart(atSea, route.id, 10_000).success)
        assertFalse(FleetSystem.purchase(atSea, "cog", 10_000).success)
        assertFalse(FleetSystem.selectShip(atSea, "cog", 10_000).success)
    }

    @Test
    fun voyageArrivesOnlyAfterAllTravelDaysAndThenAllowsTrading() {
        val route = FleetSystem.routesFrom("alex").first { it.toPortId == "ven" }
        var state = FleetSystem.depart(FleetState(), route.id, 1_000).state
        assertTrue(state.isAtSea)
        state = FleetSystem.advanceVoyageDay(state, 500).state
        assertEquals(2, state.voyage?.remainingDays)
        assertEquals("alex", state.currentPortId)
        state = FleetSystem.advanceVoyageDay(state, 500).state
        assertEquals(1, state.voyage?.remainingDays)
        state = FleetSystem.advanceVoyageDay(state, 500).state
        assertFalse(state.isAtSea)
        assertEquals("ven", state.currentPortId)
        assertFalse(FleetSystem.advanceVoyageDay(state, 500).success)
    }

    @Test
    fun restoreSanitizesUnknownShipsPortsAndInvalidVoyages() {
        val clean = FleetSystem.restore(setOf("unknown", "cog"), "unknown", "bad-port", null)
        assertEquals(setOf("cog"), clean.ownedShipIds)
        assertEquals("cog", clean.selectedShipId)
        assertEquals("alex", clean.currentPortId)
        val invalid = FleetSystem.restore(setOf("skiff"), "skiff", "alex", Voyage("alex-ven", "mus", 2))
        assertFalse(invalid.isAtSea)
        val corruptedProgress = FleetState(voyage = Voyage("alex-ven", "ven", 0))
        assertFalse(FleetSystem.advanceVoyageDay(corruptedProgress, 500).success)
        val legacySave = FleetSystem.restore(null, null, null, null)
        assertEquals(setOf("skiff"), legacySave.ownedShipIds)
        assertEquals("alex", legacySave.currentPortId)
    }
}
