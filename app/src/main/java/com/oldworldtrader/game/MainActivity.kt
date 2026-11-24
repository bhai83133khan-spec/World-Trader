package com.oldworldtrader.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OldWorldTraderApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OldWorldTraderApp() {
    val context = LocalContext.current
    val savedGame = remember(context) { GameSaveStore.load(context) }
    var gold by remember { mutableIntStateOf(savedGame?.gold ?: 12000) }
    var year by remember { mutableIntStateOf(savedGame?.year ?: 1) }
    var tab by remember { mutableIntStateOf(0) }
    var selectedCityId by remember { mutableStateOf<String?>(null) }
    var marketCityId by remember { mutableStateOf("alex") }
    var marketCityMenuExpanded by remember { mutableStateOf(false) }
    var marketState by remember { mutableStateOf(savedGame?.marketState ?: MarketState()) }
    var selectedCountry by remember { mutableStateOf(savedGame?.selectedCountry ?: "Ottoman Empire") }
    var quantity by remember { mutableIntStateOf(1) }
    var army by remember { mutableIntStateOf(savedGame?.army ?: 0) }
    var message by remember { mutableStateOf("Build your city, grow population, unlock new technologies.") }
    var cityName by remember { mutableStateOf("") }
    var cities by remember { mutableStateOf(savedGame?.cities ?: listOf(
        CityData("alex", "Alexandria", "Egypt", 1200, 6000, 20),
        CityData("ven", "Venice", "Italy", 2400, 12000, 40),
        CityData("mus", "Muscat", "Oman", 900, 4500, 15),
        CityData("del", "Delhi", "India", 1800, 8000, 25),
        CityData("bag", "Baghdad", "Iraq", 2000, 9000, 30)
    )) }
    LaunchedEffect(gold, year, army, selectedCountry, cities, marketState) {
        GameSaveStore.save(context, SavedGameState(gold, year, army, selectedCountry, cities, marketState))
    }

    val current = cities.firstOrNull { it.id == selectedCityId }
    val marketCity = cities.firstOrNull { it.id == marketCityId } ?: cities.first()
    val marketCityIndex = cities.indexOf(marketCity).coerceAtLeast(0)
    val tabs = listOf("My City", "City Travel", "Country", "Market", "Army")

    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF285743))) {
        Column(Modifier.fillMaxSize().background(Color(0xFFF3EBDD))) {
            Column(Modifier.fillMaxWidth().background(Color(0xFF1E342B)).padding(12.dp)) {
                Text("⚓ OLD WORLD TRADER", color = Color(0xFFF2D49A), fontWeight = FontWeight.Bold)
                Text("Gold $gold • Army $army • Year $year", color = Color.White)
                Text("Auto-save: local device", color = Color(0xFFBFD9CC), style = MaterialTheme.typography.labelSmall)
            }
            ScrollableTabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { index, label ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(label) })
                }
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(message)
                when (tab) {
                    0 -> {
                        if (current == null) {
                            Text("SETTLEMENTS", fontWeight = FontWeight.Bold)
                            cities.forEach { c ->
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("${c.name} • ${c.country}", fontWeight = FontWeight.Bold)
                                        Text("People: ${c.population} • City value ${c.treasuryValue}")
                                        Button(onClick = { selectedCityId = c.id }, modifier = Modifier.fillMaxWidth()) {
                                            Text("Open City")
                                        }
                                    }
                                }
                            }
                            OutlinedTextField(
                                cityName,
                                onValueChange = { cityName = it },
                                label = { Text("Name your new city") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                enabled = gold >= 2500 && cityName.trim().length >= 3,
                                onClick = {
                                    gold -= 2500
                                    val newCity = CityData(
                                        "city${cities.size + 1}", cityName.trim(), selectedCountry,
                                        300, 2500, 10, true
                                    )
                                    cities = cities + newCity
                                    cityName = ""
                                    message = "New settlement founded!"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Found city • 2,500 gold") }
                        } else {
                            Text("CITY MANAGEMENT: ${current.name}", fontWeight = FontWeight.Bold)
                            Text("Population: ${current.population} • Treasury value: ${current.treasuryValue}")
                            Text("Housing level: ${current.housingLevel}")
                            EconomyLogic.buildingSpecs.forEach { spec ->
                                val level = current.buildings[spec.name] ?: 0
                                val cost = EconomyLogic.upgradeCost(spec, level)
                                val locked = current.population < spec.unlockPopulation
                                Text("${spec.name} • Level $level • ${spec.description}")
                                Text(if (locked) "Unlock at ${spec.unlockPopulation} people" else "Upgrade cost: $cost")
                                Button(
                                    enabled = !locked && gold >= cost,
                                    onClick = {
                                        gold -= cost
                                        val updated = current.copy(
                                            buildings = current.buildings.toMutableMap(),
                                            unlocked = current.unlocked.toMutableSet()
                                        )
                                        updated.buildings[spec.name] = level + 1
                                        updated.housingLevel += 1
                                        updated.population = (updated.population.toLong() + spec.populationBoost + 80L)
                                            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                                        if (spec.name == "Power Plant") updated.unlocked.add("Electricity")
                                        cities = cities.map { if (it.id == updated.id) updated else it }
                                        message = "${spec.name} upgraded; housing and people increased."
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Build / Upgrade • ${spec.name}") }
                            }
                            Text("Unlocked: ${current.unlocked.joinToString()}")
                            Button(onClick = { selectedCityId = null }, modifier = Modifier.fillMaxWidth()) {
                                Text("Back to city list")
                            }
                        }
                    }
                    1 -> {
                        Text("CITY TRAVEL", fontWeight = FontWeight.Bold)
                        cities.forEach { c ->
                            Button(onClick = {
                                selectedCityId = c.id
                                message = "Travelled to ${c.name}"
                            }, modifier = Modifier.fillMaxWidth()) { Text("${c.name} • ${c.country}") }
                        }
                    }
                    2 -> {
                        Text("COUNTRY TRAVEL", fontWeight = FontWeight.Bold)
                        listOf("Egypt", "Italy", "Oman", "India", "Iraq", "Ottoman Empire", "Persia").forEach { country ->
                            Button(onClick = {
                                selectedCountry = country
                                message = "Travelled to $country"
                            }, modifier = Modifier.fillMaxWidth()) { Text("Travel to $country") }
                        }
                        Text("Current country: $selectedCountry")
                    }
                    3 -> {
                        Text("MARKET • ${marketCity.name}", fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = { marketCityMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Trading port: ${marketCity.name} ▼")
                        }
                        DropdownMenu(
                            expanded = marketCityMenuExpanded,
                            onDismissRequest = { marketCityMenuExpanded = false }
                        ) {
                            cities.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text("${city.name} • ${city.country}") },
                                    onClick = {
                                        marketCityId = city.id
                                        marketCityMenuExpanded = false
                                        message = "Market opened in ${city.name}."
                                    }
                                )
                            }
                        }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Button(onClick = { quantity = (quantity - 1).coerceAtLeast(1) }) { Text("−") }
                            Text("Quantity $quantity", Modifier.padding(10.dp))
                            Button(onClick = { quantity = (quantity + 1).coerceAtMost(99) }) { Text("+") }
                        }
                        Text("YOUR CARGO", fontWeight = FontWeight.Bold)
                        Text(EconomyLogic.goods.joinToString("  •  ") { good ->
                            "${good.name}: ${marketState.cargoAmount(good.name)}"
                        })
                        EconomyLogic.goods.forEach { good ->
                            val stock = marketState.stock(marketCity.id, good.name)
                            val factors = MarketSystem.factorsFor(good, marketCityIndex, year)
                            val previousFactors = MarketSystem.factorsFor(
                                good, marketCityIndex, (year - 1).coerceAtLeast(1)
                            )
                            val quote = MarketSystem.quote(
                                good = good,
                                cityIndex = marketCityIndex,
                                year = year,
                                stock = stock,
                                citySupplyFactor = factors.supplyFactor,
                                demandFactor = factors.demandFactor,
                                previousCitySupplyFactor = previousFactors.supplyFactor,
                                previousDemandFactor = previousFactors.demandFactor,
                                eventLabel = factors.label
                            )
                            val held = marketState.cargoAmount(good.name)
                            val saleValue = quote.price.toLong() * quantity.toLong()
                            val canSellSafely = MarketSystem.canSell(held, quantity) &&
                                saleValue <= Int.MAX_VALUE.toLong() &&
                                gold.toLong() + saleValue <= Int.MAX_VALUE.toLong()
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(good.name, fontWeight = FontWeight.Bold)
                                    Text("Price: ${quote.price} gold • ${quote.trendLabel} (${quote.trendPercent}%)")
                                    Text("Event: ${quote.eventLabel}")
                                    Text("Port stock: ${quote.availableStock} • In cargo: $held")
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            enabled = MarketSystem.canBuy(quote, quantity, gold),
                                            onClick = {
                                                val transaction = marketState.buy(marketCity.id, good, quote, quantity, gold)
                                                if (transaction.success) {
                                                    marketState = transaction.state
                                                    gold = transaction.gold
                                                    message = transaction.message
                                                } else message = transaction.message
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Buy") }
                                        OutlinedButton(
                                            enabled = canSellSafely,
                                            onClick = {
                                                val transaction = marketState.sell(marketCity.id, good, quote, quantity, gold)
                                                if (transaction.success) {
                                                    marketState = transaction.state
                                                    gold = transaction.gold
                                                    message = transaction.message
                                                } else message = transaction.message
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) { Text("Sell") }
                                    }
                                }
                            }
                        }
                        Text("Prices now reflect port, year, stock, and deterministic supply/demand events. Cargo and port stock are tracked separately.")
                    }
                    4 -> {
                        Text("ARMY RECRUITMENT", fontWeight = FontWeight.Bold)
                        Text("Soldiers: $army")
                        Button(
                            enabled = gold >= 100,
                            onClick = { gold -= 100; army += 10; message = "10 troops recruited" },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Recruit 10 • 100 gold") }
                        Button(
                            enabled = gold >= 500,
                            onClick = { gold -= 500; army += 60; message = "60 troops recruited" },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Recruit 60 • 500 gold") }
                        cities.filter { !it.owned }.forEach { target ->
                            Button(
                                enabled = army >= target.defense,
                                onClick = {
                                    army -= target.defense
                                    cities = cities.map { if (it.id == target.id) it.copy(owned = true) else it }
                                    message = "Conquered ${target.name}"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Conquer ${target.name} • requires ${target.defense}") }
                        }
                    }
                }
                Button(
                    onClick = {
                        year = (year.toLong() + 1L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                        marketState = marketState.restockYearly()
                        marketState = marketState.restockYearly()
                        cities = cities.map {
                            if (it.owned) it.copy(
                                population = (it.population.toLong() + EconomyLogic.populationGrowth(it).toLong())
                                    .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                            ) else it
                        }
                        val income = cities.filter { it.owned }.sumOf { it.population.toLong() / 20L }
                        gold = (gold.toLong() + income).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                        message = "Year advanced: city population growth and income collected."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Advance Year") }
            }
        }
    }
}
