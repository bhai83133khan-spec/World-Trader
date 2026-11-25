package com.oldworldtrader.game

data class ArmyRecruitmentResult(
    val success: Boolean,
    val army: Int,
    val gold: Int,
    val message: String
)

data class ArmyBattleResult(
    val valid: Boolean,
    val victory: Boolean,
    val remainingArmy: Int,
    val capturedCity: CityData?,
    val casualties: Int,
    val message: String
)

data class ArmyAnnualSettlement(
    val army: Int,
    val gold: Int,
    val income: Int,
    val upkeep: Int,
    val desertions: Int,
    val message: String
)

/** Deterministic offline army recruitment, city defense, combat and annual upkeep rules. */
object ArmySystem {
    fun recruit(army: Int, gold: Int, soldiers: Int, cost: Int): ArmyRecruitmentResult {
        if (army < 0 || gold < 0 || soldiers <= 0 || cost <= 0) {
            return ArmyRecruitmentResult(false, army.coerceAtLeast(0), gold.coerceAtLeast(0),
                "Invalid recruitment request.")
        }
        val nextArmy = army.toLong() + soldiers.toLong()
        if (nextArmy > Int.MAX_VALUE) {
            return ArmyRecruitmentResult(false, army, gold, "Army limit reached.")
        }
        if (gold < cost) {
            return ArmyRecruitmentResult(false, army, gold, "Need " + cost + " gold to recruit " + soldiers + " soldiers.")
        }
        return ArmyRecruitmentResult(true, nextArmy.toInt(), gold - cost,
            soldiers.toString() + " soldiers recruited for " + cost + " gold.")
    }

    /** City defense includes existing base defense plus defensive building bonuses. */
    fun cityDefense(city: CityData): Int {
        val barracks = (city.buildings["Army Barracks"] ?: 0).coerceAtLeast(0)
        val police = (city.buildings["Police Station"] ?: 0).coerceAtLeast(0)
        val total = city.defense.toLong().coerceAtLeast(0L) + barracks.toLong() * 20L + police.toLong() * 10L
        return total.coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
    }

    /** Minimum field force is twice the target's defense, with a floor to prevent tiny-city exploits. */
    fun minimumAttackForce(city: CityData): Int =
        (cityDefense(city).toLong() * 2L).coerceAtLeast(20L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

    /** A battle is deterministic. Valid losing attacks still consume troops; wins capture and damage the city. */
    fun attack(army: Int, target: CityData): ArmyBattleResult {
        if (army < 10) {
            return ArmyBattleResult(false, false, army.coerceAtLeast(0), null, 0,
                "At least 10 soldiers are needed to launch an attack.")
        }
        if (target.owned) {
            return ArmyBattleResult(false, false, army, null, 0, "This city is already under your control.")
        }

        val defense = cityDefense(target)
        val minimumForce = minimumAttackForce(target)
        if (army < minimumForce) {
            val casualties = (army / 4).coerceAtLeast(1).coerceAtMost(army)
            val survivors = army - casualties
            return ArmyBattleResult(true, false, survivors, null, casualties,
                "Attack on " + target.name + " failed. Lost " + casualties + " soldiers; " + survivors + " remain. Need " + minimumForce + " troops.")
        }

        val casualties = (defense / 5).coerceAtLeast(1).coerceAtMost(army)
        val survivors = army - casualties
        val damagedDefense = (target.defense.toLong().coerceAtLeast(1L) * 3L / 4L)
            .coerceAtLeast(1L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val captured = target.copy(owned = true, defense = damagedDefense)
        return ArmyBattleResult(true, true, survivors, captured, casualties,
            "Victory at " + target.name + "! Lost " + casualties + " soldiers; " + survivors + " remain. City captured with defense reduced.")
    }

    /** One gold per soldier per year. */
    fun annualUpkeep(army: Int): Int = army.coerceAtLeast(0)

    /**
     * Apply city income and army upkeep. If the treasury cannot cover upkeep,
     * one soldier deserts for each unpaid gold, up to the remaining army.
     */
    fun settleYear(army: Int, gold: Int, cityIncome: Long): ArmyAnnualSettlement {
        val safeArmy = army.coerceAtLeast(0)
        val safeGold = gold.coerceAtLeast(0)
        val safeIncome = cityIncome.coerceAtLeast(0L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val available = (safeGold.toLong() + safeIncome.toLong()).coerceAtMost(Int.MAX_VALUE.toLong())
        val upkeep = annualUpkeep(safeArmy)
        if (available >= upkeep.toLong()) {
            val remainingGold = (available - upkeep.toLong()).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            return ArmyAnnualSettlement(safeArmy, remainingGold, safeIncome, upkeep, 0,
                "Year advanced: +" + safeIncome + " city income, -" + upkeep + " army upkeep. Army maintained.")
        }
        val desertions = (upkeep.toLong() - available).coerceAtMost(safeArmy.toLong()).toInt()
        val survivors = safeArmy - desertions
        return ArmyAnnualSettlement(survivors, 0, safeIncome, upkeep, desertions,
            "Year advanced: +" + safeIncome + " city income, " + upkeep + " upkeep due. " + desertions + " soldiers deserted because upkeep was unpaid.")
    }
}
