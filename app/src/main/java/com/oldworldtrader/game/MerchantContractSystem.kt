package com.oldworldtrader.game

/** Persisted completion markers prevent replaying deterministic contracts for duplicate rewards. */
data class MerchantContractState(
    val year: Int = 0,
    val completedContractIds: Set<String> = emptySet()
) {
    fun forYear(currentYear: Int): MerchantContractState =
        if (year == currentYear) copy(completedContractIds = boundedIds(completedContractIds))
        else MerchantContractState(year = currentYear)

    companion object {
        const val MAX_SAVED_COMPLETIONS = 100

        private fun boundedIds(ids: Set<String>): Set<String> =
            ids.asSequence().filter(String::isNotBlank).sorted().takeLast(MAX_SAVED_COMPLETIONS).toSet()
    }
}

data class MerchantContract(
    val id: String,
    val destinationPortId: String,
    val goodName: String,
    val quantity: Int,
    val rewardGold: Int,
    val title: String
)

data class ContractResult(
    val success: Boolean,
    val message: String,
    val gold: Int,
    val cargo: Map<String, Int>,
    val state: MerchantContractState
)

/** Deterministic local delivery board; contracts are generated offline from port and game year. */
object MerchantContractSystem {
    private data class Template(val good: String, val quantity: Int, val baseReward: Int)

    private val templates = listOf(
        Template("Wheat", 20, 180),
        Template("Wood", 15, 240),
        Template("Iron", 10, 360),
        Template("Spices", 8, 420),
        Template("Silk", 5, 600),
        Template("Wheat", 35, 260)
    )

    fun board(portId: String, year: Int, state: MerchantContractState = MerchantContractState()): List<MerchantContract> {
        if (portId.isBlank() || year < 1) return emptyList()
        val done = state.forYear(year).completedContractIds
        val offset = Math.floorMod(portId.hashCode().toLong() + year.toLong(), templates.size.toLong()).toInt()
        return templates.indices.map { index ->
            val template = templates[(index + offset) % templates.size]
            // Reward growth is bounded so old saves or extreme years cannot overflow or create runaway payouts.
            val multiplier = 1L + (year.toLong() - 1L).coerceAtMost(20L) / 5L
            val reward = (template.baseReward.toLong() * multiplier).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            MerchantContract(
                id = "$portId-$year-$index",
                destinationPortId = portId,
                goodName = template.good,
                quantity = template.quantity,
                rewardGold = reward,
                title = "Deliver ${template.quantity} ${template.good}"
            )
        }.filterNot { it.id in done }
    }

    fun complete(
        contract: MerchantContract,
        currentPortId: String,
        year: Int,
        gold: Int,
        cargo: Map<String, Int>,
        state: MerchantContractState
    ): ContractResult {
        val safeState = state.forYear(year)
        fun reject(message: String) = ContractResult(false, message, gold, cargo, safeState)
        if (contract.id.isBlank() || contract.destinationPortId.isBlank() || contract.goodName.isBlank() ||
            contract.quantity <= 0 || contract.rewardGold < 0 || year < 1 || gold < 0
        ) return reject("Contract data is invalid.")
        if (contract.id in safeState.completedContractIds) return reject("This contract has already been completed.")
        if (currentPortId != contract.destinationPortId) return reject("Deliver this contract at ${contract.destinationPortId}.")
        val carried = (cargo[contract.goodName] ?: 0).coerceAtLeast(0)
        if (carried < contract.quantity) return reject("Not enough ${contract.goodName} in cargo.")
        val payout = gold.toLong() + contract.rewardGold.toLong()
        if (payout > Int.MAX_VALUE.toLong()) return reject("Reward exceeds safe gold limit.")

        val updatedCargo = cargo.toMutableMap()
        val remaining = carried - contract.quantity
        if (remaining == 0) updatedCargo.remove(contract.goodName) else updatedCargo[contract.goodName] = remaining
        val updatedState = safeState.copy(completedContractIds =
            (safeState.completedContractIds + contract.id).asSequence()
                .filter(String::isNotBlank).sorted().takeLast(MerchantContractState.MAX_SAVED_COMPLETIONS).toSet())
        return ContractResult(
            success = true,
            message = "Contract completed: delivered ${contract.quantity} ${contract.goodName}; earned ${contract.rewardGold} gold.",
            gold = payout.toInt(),
            cargo = updatedCargo,
            state = updatedState
        )
    }
}
