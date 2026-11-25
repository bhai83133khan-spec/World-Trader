package com.oldworldtrader.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArmySystemTest {
    private fun city(defense: Int, owned: Boolean = false, buildings: Map<String, Int> = emptyMap()) =
        CityData("target", "Target", "Italy", 1_000, 2_000, defense, owned = owned,
            buildings = buildings.toMutableMap())

    @Test
    fun recruitValidPackageAtomicallyDeductsGoldAndAddsTroops() {
        val result = ArmySystem.recruit(20, 1_000, 60, 500)
        assertTrue(result.success)
        assertEquals(80, result.army)
        assertEquals(500, result.gold)
    }

    @Test
    fun recruitmentRejectsInsufficientGoldWithoutMutation() {
        val result = ArmySystem.recruit(20, 499, 60, 500)
        assertFalse(result.success)
        assertEquals(20, result.army)
        assertEquals(499, result.gold)
    }

    @Test
    fun recruitmentRejectsInvalidPackageAndArmyOverflow() {
        assertFalse(ArmySystem.recruit(10, 100, 0, 100).success)
        assertFalse(ArmySystem.recruit(Int.MAX_VALUE - 2, 1_000, 10, 100).success)
    }

    @Test
    fun defensiveBuildingsIncreaseCityStrength() {
        assertEquals(30, ArmySystem.cityDefense(city(30)))
        assertEquals(60, ArmySystem.cityDefense(city(30, buildings = mapOf("Army Barracks" to 1, "Police Station" to 1))))
    }

    @Test
    fun attackRequiresTwiceCityDefenseWithMinimumForce() {
        assertEquals(40, ArmySystem.minimumAttackForce(city(20)))
        assertEquals(20, ArmySystem.minimumAttackForce(city(0)))
    }

    @Test
    fun winningAttackCapturesCityAndAppliesCasualtiesAndDefenseDamage() {
        val target = city(20)
        val result = ArmySystem.attack(60, target)
        assertTrue(result.valid)
        assertTrue(result.victory)
        assertEquals(4, result.casualties)
        assertEquals(56, result.remainingArmy)
        assertTrue(result.capturedCity!!.owned)
        assertEquals(15, result.capturedCity!!.defense)
        assertEquals(EnemyKingdomSystem.PLAYER_ID, result.capturedCity!!.controlledBy)
    }

    @Test
    fun captureTransfersCityFromPreviousControllerToPlayer() {
        val target = CityData("target", "Target", "Italy", 1_000, 2_000, 20,
            owned = false, controlledBy = "Rival")
        val result = ArmySystem.attack(60, target)
        assertTrue(result.victory)
        assertEquals(EnemyKingdomSystem.PLAYER_ID, result.capturedCity!!.controlledBy)
    }

    @Test
    fun playerSiegeReportSeparatesTroopLossesFromFortificationDamage() {
        val target = city(20)
        val battle = ArmySystem.attack(60, target)
        val report = ArmySystem.playerSiegeReport("Ottoman Empire", target, 60, battle)!!

        assertTrue(report.victory)
        assertTrue(report.isSiege)
        assertEquals("Ottoman Empire", report.attacker)
        assertEquals("Italy", report.defender)
        assertEquals(60, report.attackerStrength)
        assertEquals(4, report.attackerCasualties)
        assertEquals(56, report.attackerSurvivors)
        assertEquals(20, report.defenderStrength)
        assertEquals(5, report.defenderCasualties)
        assertEquals(15, report.defenderSurvivors)
        assertEquals(EnemyKingdomSystem.PLAYER_ID, report.capturedCity!!.controlledBy)
    }

    @Test
    fun failedPlayerSiegeReportShowsNoFortificationDamage() {
        val target = city(20)
        val battle = ArmySystem.attack(30, target)
        val report = ArmySystem.playerSiegeReport("Ottoman Empire", target, 30, battle)!!

        assertFalse(report.victory)
        assertTrue(report.isSiege)
        assertEquals(7, report.attackerCasualties)
        assertEquals(23, report.attackerSurvivors)
        assertEquals(20, report.defenderStrength)
        assertEquals(0, report.defenderCasualties)
        assertEquals(20, report.defenderSurvivors)
        assertNull(report.capturedCity)
    }


    @Test
    fun invalidPlayerAttackDoesNotCreateBattleReport() {
        val target = city(20)
        val battle = ArmySystem.attack(9, target)
        assertNull(ArmySystem.playerSiegeReport("Ottoman Empire", target, 9, battle))
    }


    @Test
    fun successfulSiegeDamagesDefensiveBuildingsButPreservesNonDefensiveBuildings() {
        val target = city(
            30,
            buildings = mapOf("Army Barracks" to 2, "Police Station" to 1, "House" to 3)
        )
        val result = ArmySystem.attack(200, target)
        assertTrue(result.valid)
        assertTrue(result.victory)
        val captured = result.capturedCity!!
        assertEquals(22, captured.defense)
        assertEquals(1, captured.buildings["Army Barracks"])
        assertEquals(0, captured.buildings["Police Station"])
        assertEquals(3, captured.buildings["House"])
        assertEquals(42, ArmySystem.cityDefense(captured))
        assertEquals(80, ArmySystem.cityDefense(target))
    }

    @Test
    fun failedSiegeDoesNotDamageCityBuildings() {
        val target = city(
            30,
            buildings = mapOf("Army Barracks" to 2, "Police Station" to 1)
        )
        val result = ArmySystem.attack(150, target)
        assertTrue(result.valid)
        assertFalse(result.victory)
        assertNull(result.capturedCity)
        assertEquals(2, target.buildings["Army Barracks"])
        assertEquals(1, target.buildings["Police Station"])
    }

    @Test
    fun weakAttackFailsAndConsumesTroopsButDoesNotCapture() {
        val result = ArmySystem.attack(30, city(20))
        assertTrue(result.valid)
        assertFalse(result.victory)
        assertEquals(7, result.casualties)
        assertEquals(23, result.remainingArmy)
        assertNull(result.capturedCity)
    }

    @Test
    fun attackRejectsSmallArmyAndAlreadyOwnedTarget() {
        assertFalse(ArmySystem.attack(9, city(10)).valid)
        assertFalse(ArmySystem.attack(100, city(10, owned = true)).valid)
    }

    @Test
    fun annualUpkeepIsPaidFromGoldAndCityIncome() {
        val result = ArmySystem.settleYear(80, 100, 30)
        assertEquals(80, result.army)
        assertEquals(50, result.gold)
        assertEquals(80, result.upkeep)
        assertEquals(0, result.desertions)
    }

    @Test
    fun unpaidUpkeepCausesOneDesertionPerUnpaidGold() {
        val result = ArmySystem.settleYear(100, 20, 30)
        assertEquals(50, result.army)
        assertEquals(0, result.gold)
        assertEquals(50, result.desertions)
    }

    @Test
    fun emptyArmyHasNoUpkeepAndNegativeInputsAreSafelyClamped() {
        val result = ArmySystem.settleYear(0, -50, -10)
        assertEquals(0, result.army)
        assertEquals(0, result.gold)
        assertEquals(0, result.income)
        assertEquals(0, result.upkeep)
        assertEquals(0, result.desertions)
    }
}
