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
fun FleetDashboard(
    cities: List<CityData>,
    fleetState: FleetState,
    gold: Int,
    cargo: Map<String, Int>,
    onPurchase: (String) -> Unit,
    onSelectShip: (String) -> Unit,
    onDepart: (String) -> Unit,
    onAdvanceDay: () -> Unit
) {
    val portName = cities.firstOrNull { it.id == fleetState.currentPortId }?.name ?: fleetState.currentPortId
    val selected = FleetSystem.selectedShip(fleetState)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("FLEET & SEA ROUTES", fontWeight = FontWeight.Bold)
        Text("Current port: $portName")
        Text("Cargo used: ${FleetSystem.cargoUsed(cargo)} / ${FleetSystem.selectedCapacity(fleetState)}")
        Text("Selected vessel: ${selected?.name ?: "None"}")
        FleetSystem.ships.forEach { ship ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(ship.name, fontWeight = FontWeight.Bold)
                    Text("Cost: ${ship.purchaseCost} gold • Capacity: ${ship.cargoCapacity} items")
                    if (ship.id in fleetState.ownedShipIds) {
                        Text(if (ship.id == fleetState.selectedShipId) "Currently selected" else "Owned")
                        Button(
                            enabled = !fleetState.isAtSea && ship.id != fleetState.selectedShipId,
                            onClick = { onSelectShip(ship.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (ship.id == fleetState.selectedShipId) "Selected" else "Select vessel") }
                    } else {
                        Button(
                            enabled = !fleetState.isAtSea && gold >= ship.purchaseCost,
                            onClick = { onPurchase(ship.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Buy ship • ${ship.purchaseCost} gold") }
                    }
                }
            }
        }
        if (fleetState.isAtSea) {
            val voyage = fleetState.voyage
            val destination = cities.firstOrNull { it.id == voyage?.destinationPortId }?.name ?: voyage?.destinationPortId.orEmpty()
            Text("At sea • Destination: $destination")
            Text("Days remaining: ${voyage?.remainingDays ?: 0}")
            Button(onClick = onAdvanceDay, modifier = Modifier.fillMaxWidth()) {
                Text("Advance voyage day")
            }
        } else {
            Text("AVAILABLE ROUTES", fontWeight = FontWeight.Bold)
            val routes = FleetSystem.routesFrom(fleetState.currentPortId)
            if (routes.isEmpty()) Text("No sea routes from this port.")
            routes.forEach { route ->
                val destination = cities.firstOrNull { it.id == route.toPortId }?.name ?: route.toPortId
                Button(
                    enabled = selected != null && gold >= route.travelCost,
                    onClick = { onDepart(route.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("$destination • ${route.travelDays} days • ${route.travelCost} gold")
                }
            }
        }
    }
}
