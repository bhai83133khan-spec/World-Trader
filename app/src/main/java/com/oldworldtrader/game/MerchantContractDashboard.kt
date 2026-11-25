package com.oldworldtrader.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MerchantContractDashboard(
    portId: String,
    portName: String,
    year: Int,
    state: MerchantContractState,
    cargo: Map<String, Int>,
    gold: Int,
    actionsEnabled: Boolean,
    onComplete: (MerchantContract) -> Unit
) {
    Text("MERCHANT CONTRACTS • $portName", fontWeight = FontWeight.Bold)
    Text("Contracts are generated offline for this port and year. Deliver the listed cargo here to earn the reward.")
    if (!actionsEnabled) Text("Contract deliveries are unavailable while your fleet is at sea.")
    val offers = MerchantContractSystem.board(portId, year, state)
    if (offers.isEmpty()) {
        Text("No contracts are available for this port and year.")
    } else {
        offers.forEach { contract ->
            val carried = (cargo[contract.goodName] ?: 0).coerceAtLeast(0)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(contract.title, fontWeight = FontWeight.Bold)
                    Text("Deliver at $portName • Reward ${contract.rewardGold} gold")
                    Text("Cargo: $carried / ${contract.quantity} ${contract.goodName}")
                    Button(
                        enabled = actionsEnabled && carried >= contract.quantity && gold.toLong() + contract.rewardGold.toLong() <= Int.MAX_VALUE.toLong(),
                        onClick = { onComplete(contract) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Deliver • +${contract.rewardGold} gold") }
                }
            }
        }
    }
}
