package com.oldworldtrader.game

/** Offline, deterministic merchant delivery contracts. */
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
    val cargo: Map<String, Int>
)

object MerchantContractSystem {
    private val contractTemplates = listOf(
        Triple("Wheat", 20, 180),
        Triple("Wood", 15, 240),
        Triple("Iron", 10, 360),
        Triple("Spices", 8, 420),
        Triple("Silk", 5, 600),
        Triple("Wheat", 35, 260)
    )

    /** Produces a stable contract board for a port; no randomness or network access. */
    fun board(portId: String, year: Int): List<MerchantContract> {
        if (portId.isBlank() || year < 1) return emptyList()
        val offset = Math.floorMod(portId.hashCode() + year, contractTemplates.size)
        return contractTemplates.mapIndexed { index, template ->
            val shifted = contractTemplates[(index + offset) % contractTemplates.size]
            val multiplier = 1L + (year - 1).coerceAtMost(20).toLong() / 5L
            val reward = (shifted.third.toLong() * multiplier).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            MerchantContract(
                id = "${portId}-${year}-${index}",
                destinationPortId = portId,
                goodName = shifted.first,
                quantity = shifted.second,
                rewardGold = reward,
                title = "Deliver ${shifted.second} ${shifted.first}"
            )
        }
    }

    /** Contracts pay only at their destination and only with sufficient cargo and safe gold arithmetic. */
    fun complete(
        contract: MerchantContract,
        currentPortId: String,
        gold: Int,
        cargo: Map<String, Int>
    ): ContractResult {
        if (currentPortId != contract.destinationPortId) {
            return ContractResult(false, "Deliver this contract at its destination port.", gold, cargo)
        }
        if (contract.quantity <= 0 || contract.rewardGold < 0 || gold < 0) {
            return ContractResult(false, "Contract data is invalid.", gold, cargo)
        }
        val carried = cargo[contract.goodName] ?: 0
        if (carried < contract.quantity) {
            return ContractResult(false, "Not enough ${contract.goodName} in cargo.", gold, cargo)
        }
        val newGold = gold.toLong() + contract.rewardGold.toLong()
        if (newGold > Int.MAX_VALUE) {
            return ContractResult(false, "Reward exceeds safe gold limit.", gold, cargo)
        }
        val updatedCargo = cargo.toMutableMap()
        val remaining = carried - contract.quantity
        if (remaining == 0) updatedCargo.remove(contract.goodName) else updatedCargo[contract.goodName] = remaining
        return ContractResult(true, "Contract completed: +${contract.rewardGold} gold.", newGold.toInt(), updatedCargo)
    }
}
