package com.oldworldtrader.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun CityDashboard(
    city: CityData,
    gold: Int,
    onUpgrade: (BuildingSpec) -> Unit,
    upgradeDiscountPercent: Int = 0,
    garrison: Int = 0,
    fieldArmy: Int = 0,
    onStation: (Int) -> Unit = {},
    onWithdraw: (Int) -> Unit = {}
) {
    Text("CITY DASHBOARD • ${city.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    if (!city.owned) {
        Text("This city is not under your control. You can inspect it, but cannot build here.")
    }

    val summary = listOf(
        "Population" to city.population.toString(),
        "Treasury value" to city.treasuryValue.toString(),
        "Defense strength" to CityDashboardRules.defenseStrength(city, garrison).toString(),
        "Garrison" to "$garrison soldiers",
        "Housing" to "Level ${city.housingLevel}",
        "Welfare" to CityDashboardRules.welfareLabel(city),
        "Electricity" to CityDashboardRules.electricityLabel(city)
    )
    summary.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pair.forEach { (label, value) ->
                Card(Modifier.weight(1f)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(label, style = MaterialTheme.typography.labelMedium)
                        Text(value, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (pair.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        }
    }

    val growth = CityDashboardRules.annualGrowth(city)
    val nextYearIncome = CityDashboardRules.nextYearIncome(city)
    val nextYearCityUpkeep = CityDashboardRules.nextYearCityUpkeep(city)
    val netCityIncome = CityDashboardRules.nextYearNetIncome(city)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("NEXT YEAR PREVIEW", fontWeight = FontWeight.Bold)
            Text(if (city.owned) "Population: +$growth people" else "Population: no growth while unowned")
            Text(if (city.owned) "City income: +$nextYearIncome gold" else "City income: 0 gold while unowned")
            Text(if (city.owned) "Building/service upkeep: -$nextYearCityUpkeep gold" else "Building/service upkeep: 0 gold while unowned")
            Text(if (city.owned) "Net city contribution: $netCityIncome gold" else "Net city contribution: 0 gold")
            Text("Army upkeep (1 gold per soldier/year) is charged at Advance Year.")
        }
    }

    if (city.owned) {
        val capacity = CityGarrisonSystem.capacity(city)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("CITY GARRISON", fontWeight = FontWeight.Bold)
                Text("Stationed soldiers: $garrison / $capacity")
                Text("Defense bonus: +$garrison")
                Text("Annual upkeep: ${CityGarrisonSystem.annualUpkeep(garrison)} gold")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = fieldArmy >= 10 && garrison + 10 <= capacity,
                        onClick = { onStation(10) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Station 10") }
                    Button(
                        enabled = garrison >= 10,
                        onClick = { onWithdraw(10) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Withdraw 10") }
                }
            }
        }
    }

    Text("BUILDINGS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    EconomyLogic.buildingSpecs.forEach { spec ->
        val level = (city.buildings[spec.name] ?: 0).coerceAtLeast(0)
        val cost = ResearchSystem.applyDiscount(EconomyLogic.upgradeCost(spec, level), upgradeDiscountPercent)
        val blockReason = CityDashboardRules.upgradeBlockReason(city, spec, gold, upgradeDiscountPercent)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${spec.name} • Level $level", fontWeight = FontWeight.Bold)
                Text(spec.description)
                Text("Effect: ${buildingEffect(spec)}")
                Text("Upgrade cost: $cost gold")
                if (blockReason != null) Text(blockReason, color = MaterialTheme.colorScheme.error)
                Button(
                    enabled = blockReason == null,
                    onClick = { onUpgrade(spec) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (level == 0) "Build ${spec.name}" else "Upgrade ${spec.name}")
                }
            }
        }
    }
}

private fun buildingEffect(spec: BuildingSpec): String = when (spec.name) {
    "House" -> "+${spec.populationBoost} people and +1 housing level"
    "Power Plant" -> "Unlocks electricity"
    else -> if (spec.populationBoost > 0) "+${spec.populationBoost} people" else spec.description
}
