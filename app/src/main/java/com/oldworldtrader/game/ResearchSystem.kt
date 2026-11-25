package com.oldworldtrader.game

data class ResearchSpec(
    val id: String,
    val title: String,
    val cost: Int,
    val requiredPopulation: Long,
    val prerequisiteIds: Set<String>,
    val effectDescription: String
)

data class ResearchState(val researchedTechIds: Set<String> = emptySet())

data class ResearchAction(
    val state: ResearchState,
    val gold: Int,
    val success: Boolean,
    val message: String
)

/** Offline technology progression and deterministic research effects. */
object ResearchSystem {
    const val TRADE_GUILD = "trade_guild"
    const val SHIPWRIGHT = "shipwright"
    const val MILITARY_DOCTRINE = "military_doctrine"
    const val URBAN_ENGINEERING = "urban_engineering"

    val technologies = listOf(
        ResearchSpec(TRADE_GUILD, "Trade Guild", 1_200, 0L, emptySet(),
            "Market sales earn 10% more gold; purchase prices stay unchanged."),
        ResearchSpec(SHIPWRIGHT, "Shipwright", 2_500, 800L, setOf(TRADE_GUILD),
            "Adds 40 cargo capacity to your selected vessel."),
        ResearchSpec(MILITARY_DOCTRINE, "Military Doctrine", 3_000, 1_500L, setOf(TRADE_GUILD),
            "Recruitment packages cost 15% less gold."),
        ResearchSpec(URBAN_ENGINEERING, "Urban Engineering", 2_000, 1_000L, setOf(TRADE_GUILD),
            "Building upgrades cost 10% less gold.")
    )
    private val knownIds = technologies.map { it.id }.toSet()

    fun restore(ids: Set<String>?): ResearchState =
        ResearchState(ids.orEmpty().filter { it in knownIds }.toSet())

    fun isResearched(state: ResearchState, techId: String): Boolean = techId in state.researchedTechIds

    fun isAvailable(spec: ResearchSpec, state: ResearchState, ownedPopulation: Long): Boolean =
        spec.id !in state.researchedTechIds &&
            ownedPopulation.coerceAtLeast(0L) >= spec.requiredPopulation &&
            spec.prerequisiteIds.all { it in state.researchedTechIds }

    fun research(state: ResearchState, techId: String, gold: Int, ownedPopulation: Long): ResearchAction {
        val safeState = restore(state.researchedTechIds)
        if (gold < 0) return rejected(safeState, gold, "Invalid treasury.")
        val spec = technologies.firstOrNull { it.id == techId }
            ?: return rejected(safeState, gold, "Unknown research.")
        if (spec.id in safeState.researchedTechIds) return rejected(safeState, gold, spec.title + " is already researched.")
        if (!spec.prerequisiteIds.all { it in safeState.researchedTechIds }) {
            return rejected(safeState, gold, "Research the prerequisite technologies first.")
        }
        if (ownedPopulation.coerceAtLeast(0L) < spec.requiredPopulation) {
            return rejected(safeState, gold, "Requires " + spec.requiredPopulation + " owned population.")
        }
        if (gold < spec.cost) return rejected(safeState, gold, "Need " + spec.cost + " gold to research " + spec.title + ".")
        return ResearchAction(
            ResearchState(safeState.researchedTechIds + spec.id),
            gold - spec.cost,
            true,
            spec.title + " researched! " + spec.effectDescription
        )
    }

    fun saleBonusPercent(state: ResearchState): Int = if (isResearched(state, TRADE_GUILD)) 10 else 0
    fun cargoCapacityBonus(state: ResearchState): Int = if (isResearched(state, SHIPWRIGHT)) 40 else 0
    fun recruitDiscountPercent(state: ResearchState): Int = if (isResearched(state, MILITARY_DOCTRINE)) 15 else 0
    fun buildingUpgradeDiscountPercent(state: ResearchState): Int = if (isResearched(state, URBAN_ENGINEERING)) 10 else 0

    /** Discounts use Long arithmetic and never reduce a positive cost below one gold. */
    fun applyDiscount(baseCost: Int, discountPercent: Int): Int {
        if (baseCost <= 0) return 0
        val percent = discountPercent.coerceIn(0, 100)
        val discounted = baseCost.toLong() * (100L - percent.toLong()) / 100L
        return discounted.coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
    }

    fun salePrice(basePrice: Int, state: ResearchState): Int {
        if (basePrice <= 0) return 0
        val percent = saleBonusPercent(state).coerceIn(0, 100)
        val increased = basePrice.toLong() * (100L + percent.toLong()) / 100L
        return increased.coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
    }

    fun recruitCost(baseCost: Int, state: ResearchState): Int =
        applyDiscount(baseCost, recruitDiscountPercent(state))

    private fun rejected(state: ResearchState, gold: Int, message: String) =
        ResearchAction(state, gold, false, message)
}
