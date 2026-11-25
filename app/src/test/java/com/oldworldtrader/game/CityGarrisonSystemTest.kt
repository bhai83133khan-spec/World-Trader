package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CityGarrisonSystemTest {
    private fun city(id: String = "alex", owned: Boolean = true, barracks: Int = 0, controller: String? = null) =
        CityData(id, if (id == "alex") "Alexandria" else "Venice", "Egypt", 1_000, 2_000, 20, owned = owned,
            buildings = mutableMapOf("Army Barracks" to barracks), controlledBy = controller)

    @Test
    fun capacityStartsAtOneHundredAndBarracksAddFiftyPerLevel() {
        assertEquals(100, CityGarrisonSystem.capacity(city()))
        assertEquals(200, CityGarrisonSystem.capacity(city(barracks = 2)))
        assertEquals(CityGarrisonSystem.MAX_CAPACITY, CityGarrisonSystem.capacity(city(barracks = Int.MAX_VALUE)))
    }

    @Test
    fun maxStationingRespectsArmyAndCityCapacity() {
        assertEquals(80, CityGarrisonSystem.maxStationable(80, 20, city()))
        assertEquals(15, CityGarrisonSystem.maxStationable(80, 85, city()))
        assertEquals(0, CityGarrisonSystem.maxStationable(80, 100, city()))
        assertEquals(0, CityGarrisonSystem.maxStationable(Int.MAX_VALUE, 101, city()))
        assertEquals(0, CityGarrisonSystem.maxStationable(80, 0, city(owned = false)))
    }

    @Test
    fun maxWithdrawalAllowsPartialTransferAtFieldArmyIntegerLimit() {
        assertEquals(30, CityGarrisonSystem.maxWithdrawable(20, 30, city()))
        assertEquals(5, CityGarrisonSystem.maxWithdrawable(Int.MAX_VALUE - 5, 30, city()))
        assertEquals(0, CityGarrisonSystem.maxWithdrawable(Int.MAX_VALUE, 30, city()))
        assertEquals(0, CityGarrisonSystem.maxWithdrawable(-1, 30, city()))
        assertEquals(0, CityGarrisonSystem.maxWithdrawable(0, 30, city(controller = "Rival")))
    }

    @Test
    fun transferMovesTroopsBetweenControlledCitiesWithoutChangingTotal() {
        val alex = city("alex", barracks = 1)
        val venice = city("ven", barracks = 1)
        val original = mapOf("alex" to 60, "ven" to 20)
        val result = CityGarrisonSystem.transfer(original, listOf(alex, venice), "alex", "ven", 30)

        assertTrue(result.success)
        assertEquals(30, result.transferred)
        assertEquals(mapOf("alex" to 30, "ven" to 50), result.garrisons)
        assertEquals(CityGarrisonSystem.total(original), CityGarrisonSystem.total(result.garrisons))
    }

    @Test
    fun transferRejectsOwnershipCapacityAmountsAndInvalidStateWithoutMutation() {
        val home = city("alex", barracks = 1)
        val destination = city("ven")
        val rival = destination.copy(owned = true, controlledBy = "Rival")
        val original = mapOf("alex" to 60, "ven" to 95)
        val cities = listOf(home, destination)

        assertFalse(CityGarrisonSystem.transfer(original, cities, "alex", "alex", 1).success)
        assertFalse(CityGarrisonSystem.transfer(original, cities, "alex", "missing", 1).success)
        assertFalse(CityGarrisonSystem.transfer(original, listOf(home, rival), "alex", "ven", 1).success)
        assertFalse(CityGarrisonSystem.transfer(original, cities, "alex", "ven", 6).success)
        assertFalse(CityGarrisonSystem.transfer(original, cities, "alex", "ven", 61).success)
        assertFalse(CityGarrisonSystem.transfer(original, cities, "alex", "ven", 0).success)
        assertFalse(CityGarrisonSystem.transfer(mapOf("alex" to 151, "ven" to 0), cities, "alex", "ven", 1).success)
        assertEquals(mapOf("alex" to 60, "ven" to 95), original)
    }

    @Test
    fun stationingTransfersSoldiersWithoutCreatingOrDestroyingTroops() {
        val result = CityGarrisonSystem.station(80, 0, 10, city())
        assertTrue(result.success)
        assertEquals(70, result.fieldArmy)
        assertEquals(10, result.garrison)
        assertEquals(10, result.transferred)
    }

    @Test
    fun stationingRejectsUnownedCityInsufficientArmyAndCapacityWithoutMutation() {
        val unowned = CityGarrisonSystem.station(80, 0, 10, city(owned = false))
        assertFalse(unowned.success)
        assertEquals(80, unowned.fieldArmy)
        val shortArmy = CityGarrisonSystem.station(5, 0, 10, city())
        assertFalse(shortArmy.success)
        assertEquals(5, shortArmy.fieldArmy)
        val full = CityGarrisonSystem.station(500, 95, 10, city())
        assertFalse(full.success)
        assertEquals(95, full.garrison)
    }

    @Test
    fun stationingRejectsInvalidAmountsAndCorruptState() {
        assertFalse(CityGarrisonSystem.station(-1, 0, 10, city()).success)
        assertFalse(CityGarrisonSystem.station(50, -1, 10, city()).success)
        assertFalse(CityGarrisonSystem.station(50, 0, 0, city()).success)
        assertFalse(CityGarrisonSystem.station(50, 101, 1, city()).success)
    }

    @Test
    fun withdrawingTransfersTroopsBackAndRejectsOverflowOrInsufficientGarrison() {
        val ok = CityGarrisonSystem.withdraw(20, 40, 10, city())
        assertTrue(ok.success)
        assertEquals(30, ok.fieldArmy)
        assertEquals(30, ok.garrison)
        assertFalse(CityGarrisonSystem.withdraw(20, 9, 10, city()).success)
        assertFalse(CityGarrisonSystem.withdraw(Int.MAX_VALUE - 5, 10, 10, city()).success)
    }

    @Test
    fun rivalCitiesCannotReceiveGarrisonTransfers() {
        assertFalse(CityGarrisonSystem.station(100, 0, 10, city(controller = "Rival")).success)
        assertEquals(0, CityGarrisonSystem.capacity(city(owned = false)))
    }

    @Test
    fun garrisonUpkeepIsOneGoldPerSoldierAndNegativeStateIsClamped() {
        assertEquals(35, CityGarrisonSystem.annualUpkeep(35))
        assertEquals(0, CityGarrisonSystem.annualUpkeep(-10))
    }

    @Test
    fun saveSanitizationKeepsOnlyOwnedCityGarrisonsAndClampsCapacity() {
        val home = city(barracks = 1)
        val rival = CityData("ven", "Venice", "Italy", 1_000, 1_000, 20, owned = true, controlledBy = "Rival")
        assertEquals(mapOf("alex" to 150), CityGarrisonSystem.sanitize(
            mapOf("alex" to 300, "ven" to 50, "missing" to 10, "bad" to -1),
            listOf(home, rival)
        ))
    }

    @Test
    fun desertionsAreDeterministicAndReduceLargestGarrisonFirst() {
        val original = mapOf("ven" to 20, "alex" to 30, "bag" to 10)
        assertEquals(mapOf("alex" to 5, "ven" to 20, "bag" to 10),
            CityGarrisonSystem.applyDesertions(original, 25))
        assertEquals(mapOf("bag" to 10), CityGarrisonSystem.applyDesertions(original, 50))
    }
}
