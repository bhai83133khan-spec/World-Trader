package com.oldworldtrader.game

/** A kingdom that acts independently during the yearly world simulation. */
data class EnemyKingdom(
    val id: String,
    val army: Int,
    val treasury: Int,
    val aggression: Int = 35
)

data class EnemyKingdomState(
    val kingdoms: Map<String, EnemyKingdom> = emptyMap(),
    val lastProcessedYear: Int = 0
)

data class BattleReport(
    val attacker: String,
    val defender: String,
    val attackerStrength: Int,
    val defenderStrength: Int,
    val attackerCasualties: Int,
    val defenderCasualties: Int,
    val attackerSurvivors: Int,
    val defenderSurvivors: Int,
    val loot: Int,
    val capturedCity: CityData?,
    val victory: Boolean,
    val summary: String
)

data class BattleResolution(
    val report: BattleReport,
    val attackerArmy: Int,
    val defenderArmy: Int,
    val gold: Int
)

data class EnemyTurnResult(
    val state: EnemyKingdomState,
    val cities: List<CityData>,
    val reports: List<BattleReport>
)

/** Deterministic combat and offline AI turns. A given state and year always produce the same result. */
object EnemyKingdomSystem {
    const val PLAYER_ID = "player"
    private const val MAX_ARMY = 2_000_000_000

    /** Build compatible initial AI state from the existing city list and territory fields. */
    fun initialize(cities: List<CityData>, playerKingdom: String): EnemyKingdomState {
        val kingdoms = cities.asSequence()
            .filter { !it.owned && it.country != playerKingdom && it.controlledBy == null }
            .groupBy { it.country }
            .mapValues { (id, holdings) ->
                val seedArmy = holdings.sumOf { ArmySystem.cityDefense(it).toLong() * 2L }
                    .coerceIn(40L, MAX_ARMY.toLong()).toInt()
                val seedTreasury = holdings.sumOf { it.treasuryValue.toLong() / 4L }
                    .coerceIn(500L, Int.MAX_VALUE.toLong()).toInt()
                EnemyKingdom(id, seedArmy, seedTreasury, aggression = 25 + stableHash(id) % 46)
            }
        return EnemyKingdomState(kingdoms)
    }

    /** Resolve at most one attack per enemy kingdom and year; repeated calls are idempotent. */
    fun advanceYear(
        state: EnemyKingdomState,
        cities: List<CityData>,
        year: Int,
        playerKingdom: String
    ): EnemyTurnResult {
        if (year <= state.lastProcessedYear || year <= 0) {
            return EnemyTurnResult(state, cities, emptyList())
        }
        val initialized = initialize(cities, playerKingdom)
        val kingdoms = (initialized.kingdoms + state.kingdoms).toMutableMap()
        var updatedCities = cities
        val reports = mutableListOf<BattleReport>()

        kingdoms.toSortedMap().forEach { (kingdomId, original) ->
            val kingdomCities = updatedCities.filter { it.country == kingdomId && !it.owned && it.controlledBy == null }
            if (kingdomCities.isEmpty()) return@forEach
            val population = kingdomCities.sumOf { it.population.toLong().coerceAtLeast(0L) }
            val reinforcements = (population / 500L).coerceIn(2L, 10_000L)
            val income = (population / 30L).coerceIn(5L, 1_000_000L)
            val army = (original.army.toLong().coerceAtLeast(0L) + reinforcements)
                .coerceAtMost(MAX_ARMY.toLong()).toInt()
            val treasury = (original.treasury.toLong().coerceAtLeast(0L) + income)
                .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            var kingdom = original.copy(army = army, treasury = treasury)

            val targets = updatedCities.filter { it.owned && it.controlledBy == PLAYER_ID }
                .sortedWith(compareBy<CityData> { ArmySystem.cityDefense(it) }.thenBy { it.id })
            val target = targets.firstOrNull()
            if (target != null && army >= ArmySystem.minimumAttackForce(target) && shouldAttack(kingdomId, year, kingdom.aggression)) {
                val garrison = defenderGarrison(target)
                val resolution = resolveBattle(
                    attacker = kingdomId,
                    defender = playerKingdom,
                    attackerArmy = army,
                    defenderArmy = garrison,
                    city = target,
                    gold = 0,
                    attackerIsPlayer = false,
                    lootAvailable = treasury
                )
                reports += resolution.report
                kingdom = kingdom.copy(
                    army = resolution.attackerArmy,
                    treasury = (treasury - resolution.report.loot).coerceAtLeast(0)
                )
                resolution.report.capturedCity?.let { captured ->
                    updatedCities = updatedCities.map { if (it.id == target.id) captured else it }
                }
            }
            kingdoms[kingdomId] = kingdom
        }
        return EnemyTurnResult(
            EnemyKingdomState(kingdoms, year), updatedCities, reports
        )
    }

    fun resolveBattle(
        attacker: String,
        defender: String,
        attackerArmy: Int,
        defenderArmy: Int,
        city: CityData?,
        gold: Int,
        attackerIsPlayer: Boolean,
        lootAvailable: Int = 0
    ): BattleResolution {
        val attacking = attackerArmy.coerceAtLeast(0)
        val defending = defenderArmy.coerceAtLeast(0)
        val fortification = city?.let(ArmySystem::cityDefense) ?: 0
        val attackStrength = attacking
        val defenseStrength = (defending.toLong() + fortification.toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val victory = attackStrength > defenseStrength

        val attackerLosses = if (attacking == 0) 0 else if (victory) {
            (defenseStrength / 12).coerceAtLeast(1).coerceAtMost(attacking)
        } else {
            (attacking / 3).coerceAtLeast(1).coerceAtMost(attacking)
        }
        val defenderLosses = if (defending == 0) 0 else if (victory) {
            (defending / 2).coerceAtLeast(1).coerceAtMost(defending)
        } else {
            (attacking / 5).coerceAtLeast(1).coerceAtMost(defending)
        }
        val attackerSurvivors = attacking - attackerLosses
        val defenderSurvivors = defending - defenderLosses
        val loot = if (victory) {
            (attacking.toLong() * 3L).coerceAtMost(lootAvailable.coerceAtLeast(0).toLong()).toInt()
        } else 0
        val captured = if (victory && city != null) city.copy(
            owned = attackerIsPlayer,
            controlledBy = if (attackerIsPlayer) PLAYER_ID else attacker
        ) else null
        val place = city?.name?.let { " at $it" }.orEmpty()
        val summary = if (victory) {
            "$attacker prevailed over $defender$place, seized $loot gold, and ${if (city != null) "captured the city" else "held the field"}."
        } else {
            "$attacker was repelled by $defender$place; the defenders held their ground."
        }
        val report = BattleReport(
            attacker, defender, attackStrength, defenseStrength,
            attackerLosses, defenderLosses, attackerSurvivors, defenderSurvivors,
            loot, captured, victory, summary
        )
        val nextGold = if (attackerIsPlayer) {
            (gold.toLong().coerceAtLeast(0L) + loot.toLong()).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        } else gold.coerceAtLeast(0)
        return BattleResolution(report, attackerSurvivors, defenderSurvivors, nextGold)
    }

    private fun defenderGarrison(city: CityData): Int =
        (city.population.toLong().coerceAtLeast(0L) / 100L + ArmySystem.cityDefense(city).toLong() * 2L)
            .coerceIn(10L, MAX_ARMY.toLong()).toInt()

    private fun shouldAttack(kingdom: String, year: Int, aggression: Int): Boolean {
        val roll = Math.floorMod(stableHash("$kingdom:$year"), 100)
        return roll < aggression.coerceIn(0, 100)
    }

    private fun stableHash(value: String): Int = value.fold(17) { hash, char -> hash * 31 + char.code }
}
