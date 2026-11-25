package com.oldworldtrader.game

/** Ship and port travel rules for the offline sea-trading game. */
data class ShipSpec(
    val id: String,
    val name: String,
    val purchaseCost: Int,
    val cargoCapacity: Int
)

data class SeaRoute(
    val id: String,
    val fromPortId: String,
    val toPortId: String,
    val travelDays: Int,
    val travelCost: Int
)

data class Voyage(
    val routeId: String,
    val destinationPortId: String,
    val remainingDays: Int
)

data class FleetState(
    val ownedShipIds: Set<String> = setOf("skiff"),
    val selectedShipId: String = "skiff",
    val currentPortId: String = "alex",
    val voyage: Voyage? = null,
    val shipUpgradeLevels: Map<String, Int> = emptyMap()
) {
    val isAtSea: Boolean get() = voyage != null
}

data class FleetAction(
    val state: FleetState,
    val gold: Int,
    val success: Boolean,
    val message: String
)

object FleetSystem {
    val ships = listOf(
        ShipSpec("skiff", "Starter Skiff", 0, 30),
        ShipSpec("sloop", "Coastal Sloop", 3_000, 60),
        ShipSpec("cog", "Merchant Cog", 6_500, 160),
        ShipSpec("carrack", "Great Carrack", 12_000, 320)
    )

    private val portIds = setOf("alex", "ven", "mus", "bag", "del")
    private val oneWayRoutes = listOf(
        SeaRoute("alex-ven", "alex", "ven", 3, 450),
        SeaRoute("alex-mus", "alex", "mus", 2, 320),
        SeaRoute("mus-bag", "mus", "bag", 2, 350),
        SeaRoute("bag-del", "bag", "del", 3, 550),
        SeaRoute("ven-del", "ven", "del", 4, 800)
    )
    val routes: List<SeaRoute> = oneWayRoutes.flatMap { route ->
        listOf(
            route,
            route.copy(
                id = "${route.toPortId}-${route.fromPortId}",
                fromPortId = route.toPortId,
                toPortId = route.fromPortId
            )
        )
    }

    fun shipById(id: String?): ShipSpec? = ships.firstOrNull { it.id == id }

    fun selectedShip(state: FleetState): ShipSpec? =
        shipById(state.selectedShipId)?.takeIf { it.id in state.ownedShipIds }

    const val MAX_SHIP_UPGRADE_LEVEL = 3

    fun shipCapacity(state: FleetState, shipId: String): Int {
        val ship = shipById(shipId) ?: return 0
        val level = state.shipUpgradeLevels[shipId]?.coerceIn(0, MAX_SHIP_UPGRADE_LEVEL) ?: 0
        val increment = ship.cargoCapacity / 4
        return (ship.cargoCapacity.toLong() + increment.toLong() * level)
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    fun selectedCapacity(state: FleetState): Int =
        selectedShip(state)?.let { shipCapacity(state, it.id) } ?: 0

    /** Next cargo-hull upgrade price; null when unowned or fully upgraded. */
    fun upgradeCost(state: FleetState, shipId: String): Int? {
        val ship = shipById(shipId) ?: return null
        if (shipId !in state.ownedShipIds) return null
        val level = state.shipUpgradeLevels[shipId]?.coerceIn(0, MAX_SHIP_UPGRADE_LEVEL) ?: 0
        if (level >= MAX_SHIP_UPGRADE_LEVEL) return null
        val baseCost = maxOf(500L, ship.purchaseCost.toLong() / 4L)
        return (baseCost * (level + 1L)).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    fun upgradeShip(state: FleetState, shipId: String, gold: Int): FleetAction {
        if (state.isAtSea) return rejected(state, gold, "You cannot upgrade ships while at sea.")
        if (gold < 0) return rejected(state, gold, "Invalid wallet.")
        if (shipId !in state.ownedShipIds) return rejected(state, gold, "You do not own that ship.")
        val ship = shipById(shipId) ?: return rejected(state, gold, "Unknown ship.")
        val level = state.shipUpgradeLevels[shipId]?.coerceIn(0, MAX_SHIP_UPGRADE_LEVEL) ?: 0
        if (level >= MAX_SHIP_UPGRADE_LEVEL) return rejected(state, gold, "${ship.name} has reached maximum cargo-hull level.")
        val cost = upgradeCost(state, shipId) ?: return rejected(state, gold, "Upgrade unavailable.")
        if (gold < cost) return rejected(state, gold, "Need $cost gold for ${ship.name}'s cargo-hull upgrade.")
        val nextLevels = state.shipUpgradeLevels + (shipId to (level + 1))
        val nextState = state.copy(shipUpgradeLevels = nextLevels)
        return FleetAction(
            nextState,
            gold - cost,
            true,
            "${ship.name} upgraded to cargo-hull level ${level + 1}. Capacity: ${shipCapacity(nextState, shipId)}."
        )
    }

    fun cargoUsed(cargo: Map<String, Int>): Int =
        cargo.values.sumOf { it.coerceAtLeast(0).toLong() }
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

    /** Total cargo weight is measured as item count; negative saved quantities are ignored. */
    fun canFitCargo(cargo: Map<String, Int>, goodName: String, quantity: Int, capacity: Int): Boolean {
        if (goodName.isBlank() || quantity <= 0 || capacity < 0) return false
        val used = cargoUsed(cargo).toLong()
        return used <= capacity.toLong() && used + quantity.toLong() <= capacity.toLong()
    }

    fun purchase(state: FleetState, shipId: String, gold: Int): FleetAction {
        val ship = shipById(shipId) ?: return rejected(state, gold, "Unknown ship.")
        if (state.isAtSea) return rejected(state, gold, "You cannot buy ships while at sea.")
        if (gold < 0) return rejected(state, gold, "Invalid wallet.")
        if (ship.id in state.ownedShipIds) return rejected(state, gold, "You already own this ship.")
        if (gold < ship.purchaseCost) return rejected(state, gold, "Need ${ship.purchaseCost} gold to buy ${ship.name}.")
        return FleetAction(
            state.copy(ownedShipIds = state.ownedShipIds + ship.id),
            gold - ship.purchaseCost,
            true,
            "Purchased ${ship.name}. Select it in your fleet."
        )
    }

    fun selectShip(state: FleetState, shipId: String, gold: Int): FleetAction {
        if (state.isAtSea) return rejected(state, gold, "You cannot change ships while at sea.")
        if (shipId !in state.ownedShipIds || shipById(shipId) == null)
            return rejected(state, gold, "You do not own that ship.")
        return FleetAction(state.copy(selectedShipId = shipId), gold, true, "Selected ${shipById(shipId)?.name}.")
    }

    fun routesFrom(portId: String): List<SeaRoute> = routes.filter { it.fromPortId == portId }

    fun depart(state: FleetState, routeId: String, gold: Int): FleetAction {
        if (state.isAtSea) return rejected(state, gold, "You are already sailing.")
        if (selectedShip(state) == null) return rejected(state, gold, "Select an owned ship first.")
        val route = routes.firstOrNull { it.id == routeId && it.fromPortId == state.currentPortId }
            ?: return rejected(state, gold, "That route is not available from this port.")
        if (gold < 0 || route.travelCost < 0) return rejected(state, gold, "Invalid wallet or route cost.")
        if (gold < route.travelCost) return rejected(state, gold, "Need ${route.travelCost} gold for this voyage.")
        return FleetAction(
            state.copy(voyage = Voyage(route.id, route.toPortId, route.travelDays)),
            gold - route.travelCost,
            true,
            "Sailing to ${route.toPortId}. Voyage: ${route.travelDays} days."
        )
    }

    fun advanceVoyageDay(state: FleetState, gold: Int): FleetAction {
        val voyage = state.voyage ?: return rejected(state, gold, "No active voyage.")
        val route = routes.firstOrNull { it.id == voyage.routeId && it.fromPortId == state.currentPortId && it.toPortId == voyage.destinationPortId }
            ?: return rejected(state, gold, "Invalid voyage route.")
        if (voyage.remainingDays !in 1..route.travelDays) return rejected(state, gold, "Invalid voyage progress.")
        if (voyage.remainingDays > 1) {
            val updated = voyage.copy(remainingDays = voyage.remainingDays - 1)
            return FleetAction(state.copy(voyage = updated), gold, true, "At sea: ${updated.remainingDays} day(s) remaining.")
        }
        return FleetAction(
            state.copy(currentPortId = voyage.destinationPortId, voyage = null),
            gold,
            true,
            "Arrived at ${voyage.destinationPortId}."
        )
    }

    /** Sanitize optional fleet data from older or partially corrupted offline saves. */
    fun restore(
        ownedShipIds: Set<String>?,
        selectedShipId: String?,
        currentPortId: String?,
        voyage: Voyage?,
        shipUpgradeLevels: Map<String, Int>? = null
    ): FleetState {
        val fallback = FleetState()
        val owned = ships.map { it.id }.filter { it in ownedShipIds.orEmpty() }.toSet().ifEmpty { fallback.ownedShipIds }
        val selected = selectedShipId?.takeIf { it in owned } ?: ships.first { it.id in owned }.id
        val port = currentPortId?.takeIf { it in portIds } ?: fallback.currentPortId
        val route = voyage?.let { candidate -> routes.firstOrNull { it.id == candidate.routeId } }
        val safeVoyage = voyage?.takeIf { candidate ->
            route != null &&
                route.fromPortId == port &&
                route.toPortId == candidate.destinationPortId &&
                candidate.destinationPortId in portIds &&
                candidate.remainingDays in 1..route.travelDays
        }
        val safeUpgradeLevels = shipUpgradeLevels.orEmpty().mapNotNull { (shipId, level) ->
            if (shipId in owned && level in 1..MAX_SHIP_UPGRADE_LEVEL) shipId to level else null
        }.toMap()
        return FleetState(owned, selected, port, safeVoyage, safeUpgradeLevels)
    }

    private fun rejected(state: FleetState, gold: Int, message: String) =
        FleetAction(state, gold, false, message)
}
