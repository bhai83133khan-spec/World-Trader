package com.oldworldtrader.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ResearchDashboard(
    state: ResearchState,
    gold: Int,
    ownedPopulation: Long,
    onResearch: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("RESEARCH & TECHNOLOGY", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Owned population: " + ownedPopulation)
        Text("Research is permanent, offline, and costs gold once. Effects are applied to market sales, ships, recruitment, and building upgrades.")
        ResearchSystem.technologies.forEach { tech ->
            val complete = ResearchSystem.isResearched(state, tech.id)
            val available = ResearchSystem.isAvailable(tech, state, ownedPopulation)
            val missingPrerequisites = tech.prerequisiteIds
                .filter { it !in state.researchedTechIds }
                .mapNotNull { id -> ResearchSystem.technologies.firstOrNull { it.id == id }?.title }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(tech.title, fontWeight = FontWeight.Bold)
                    Text(tech.effectDescription)
                    Text("Cost: " + tech.cost + " gold")
                    Text("Owned population required: " + tech.requiredPopulation)
                    if (missingPrerequisites.isNotEmpty()) {
                        Text("Prerequisites: " + missingPrerequisites.joinToString(", "))
                    }
                    Text(
                        when {
                            complete -> "Completed"
                            available && gold >= tech.cost -> "Ready to research"
                            available -> "Need " + tech.cost + " gold"
                            else -> "Locked"
                        },
                        color = if (complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        enabled = !complete && available && gold >= tech.cost,
                        onClick = { onResearch(tech.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (complete) "Researched" else "Research • " + tech.cost + " gold")
                    }
                }
            }
        }
    }
}
