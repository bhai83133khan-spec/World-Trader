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
    var fleetState by remember { mutableStateOf(savedGame?.fleetState ?: FleetState()) }
    val savedMarketCityId = savedGame?.let { state ->
        CityTravelSystem.restoreMarketCityId(state.marketCityId, state.cities, state.fleetState.currentPortId)
    }
    var marketCityId by remember { mutableStateOf(savedMarketCityId ?: "alex") }
    var marketState by remember { mutableStateOf(savedGame?.marketState ?: MarketState()) }
    var shopState by remember { mutableStateOf(savedGame?.shopState ?: ShopState()) }
    var shopCityId by remember { mutableStateOf(savedGame?.cities?.firstOrNull()?.id ?: "alex") }
    var selectedCountry by remember { mutableStateOf(savedGame?.selectedCountry ?: "Ottoman Empire") }
    var quantity by remember { mutableIntStateOf(1) }
    var army by remember { mutableIntStateOf(savedGame?.army ?: 0) }
    var researchState by remember { mutableStateOf(savedGame?.researchState ?: ResearchState()) }
    var message by remember { mutableStateOf("Build your city, grow population, unlock new technologies.") }
    var cityName by remember { mutableStateOf("") }
    var cities by remember { mutableStateOf(savedGame?.cities ?: listOf(
        CityData("alex", "Alexandria", "Egypt", 1200, 6000, 20),
        CityData("ven", "Venice", "Italy", 2400, 12000, 40),
        CityData("mus", "Muscat", "Oman", 900, 4500, 15),
        CityData("del", "Delhi", "India", 1800, 8000, 25),
        CityData("bag", "Baghdad", "Iraq", 2000, 9000, 30)
    )) }
    var enemyKingdomState by remember { mutableStateOf(savedGame?.enemyKingdomState ?: EnemyKingdomState()) }
    var battleReports by remember { mutableStateOf(savedGame?.battleReports ?: emptyList()) }
    LaunchedEffect(gold, year, army, selectedCountry, cities, marketState, shopState, fleetState, researchState, marketCityId, enemyKingdomState, battleReports) {
        GameSaveStore.save(
            context,
            SavedGameState(
                gold = gold,
                year = year,
                army = army,
                selectedCountry = selectedCountry,
                cities = cities,
                marketState = marketState,
                shopState = shopState,
                fleetState = fleetState,
                researchState = researchState,
                marketCityId = marketCityId,
                enemyKingdomState = enemyKingdomState,
                battleReports = battleReports
            )
        )
    }

    val current = cities.firstOrNull { it.id == selectedCityId }
    val marketCity = cities.firstOrNull { it.id == marketCityId } ?: cities.first()
    val marketCityIndex = cities.indexOf(marketCity).coerceAtLeast(0)
    val kingdomSnapshot = KingdomProgression.evaluate(cities)
    val cityFoundingCost = KingdomProgression.foundingCost(cities)
    val tabs = listOf("My City", "City Travel", "Country", "Market", "Shop", "Army", "Fleet", "Kingdom", "Research", "War Reports")

    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF285743))) {
        Column(Modifier.fillMaxSize().background(Color(0xFFF3EBDD))) {
            Column(Modifier.fillMaxWidth().background(Color(0xFF1E342B)).padding(12.dp)) {
                Text("⚓ OLD WORLD TRADER", color = Color(0xFFF2D49A), fontWeight = FontWeight.Bold)
                Text("Gold $gold • Army $army • Year $year", color = Color.White)
                Text("Kingdom ${kingdomSnapshot.currentRank.title} • Founding discount ${kingdomSnapshot.currentRank.foundingDiscountPercent}%", color = Color.White)
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
                                enabled = gold >= cityFoundingCost && EconomyLogic.canFoundCityName(cityName, cities),
                                onClick = {
                                    gold -= cityFoundingCost
                                    val newCity = CityData(
                                        EconomyLogic.nextCityId(cities), cityName.trim(), selectedCountry,
                                        300, cityFoundingCost, 10, true
                                    )
                                    cities = cities + newCity
                                    cityName = ""
                                    message = "New settlement founded for ${cityFoundingCost} gold. Kingdom rank: ${kingdomSnapshot.currentRank.title}."
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Found city • ${cityFoundingCost} gold") }
                        } else {
                            CityDashboard(
                                city = current,
                                gold = gold,
                                upgradeDiscountPercent = ResearchSystem.buildingUpgradeDiscountPercent(researchState),
                                onUpgrade = { spec ->
                                    val reason = CityDashboardRules.upgradeBlockReason(current, spec, gold, ResearchSystem.buildingUpgradeDiscountPercent(researchState))
                                    if (reason != null) {
                                        message = reason
                                    } else {
                                        val cost = ResearchSystem.applyDiscount(EconomyLogic.upgradeCost(spec, current.buildings[spec.name] ?: 0), ResearchSystem.buildingUpgradeDiscountPercent(researchState))
                                        val updated = EconomyLogic.applyUpgrade(current, spec)
                                        gold -= cost
                                        cities = cities.map { if (it.id == updated.id) updated else it }
                                        message = "${spec.name} upgraded in ${current.name}."
                                    }
                                }
                            )
                            Button(onClick = { selectedCityId = null }, modifier = Modifier.fillMaxWidth()) {
                                Text("Back to settlements")
                            }
                        }
                    }
                    1 -> {
                        Text("CITY TRAVEL", fontWeight = FontWeight.Bold)
                        if (fleetState.isAtSea) {
                            Text("City travel is unavailable while your fleet is at sea. Complete the voyage first.")
                        }
                        cities.forEach { c ->
                            val visit = CityTravelSystem.visit(c, fleetState.isAtSea)
                            Button(
                                enabled = visit.success,
                                onClick = {
                                    val action = CityTravelSystem.visit(c, fleetState.isAtSea)
                                    if (action.success) {
                                        action.cityId?.let { cityId ->
                                            selectedCityId = cityId
                                            marketCityId = cityId
                                            tab = 0
                                        }
                                    }
                                    message = action.message
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Visit ${c.name} • ${c.country}") }
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
                        Text("Trading port: ${marketCity.name}")
                        if (!fleetState.isAtSea && marketCityId != fleetState.currentPortId) {
                            val fleetPortName = cities.firstOrNull { it.id == fleetState.currentPortId }?.name ?: fleetState.currentPortId
                            Text("Visited market • Fleet remains docked at $fleetPortName")
                        }
                        Text("Cargo capacity: ${FleetSystem.cargoUsed(marketState.cargo)} / ${FleetSystem.selectedCapacity(fleetState) + ResearchSystem.cargoCapacityBonus(researchState)}")
                        if (fleetState.isAtSea) {
                            Text("Trading is locked while your ship is sailing.")
                            Button(
                                onClick = {
                                    val action = FleetSystem.advanceVoyageDay(fleetState, gold)
                                    if (action.success) {
                                        fleetState = action.state
                                        if (!action.state.isAtSea) marketCityId = action.state.currentPortId
                                    }
                                    message = action.message
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Advance voyage day • ${fleetState.voyage?.remainingDays ?: 0} left") }
                        } else {
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
                                val saleQuote = quote.copy(price = ResearchSystem.salePrice(quote.price, researchState))
                                val held = marketState.cargoAmount(good.name)
                                val saleValue = saleQuote.price.toLong() * quantity.toLong()
                                val capacity = FleetSystem.selectedCapacity(fleetState) + ResearchSystem.cargoCapacityBonus(researchState)
                                val cargoFits = FleetSystem.canFitCargo(marketState.cargo, good.name, quantity, capacity)
                                val canSellSafely = MarketSystem.canSell(held, quantity) &&
                                    saleValue <= Int.MAX_VALUE.toLong() &&
                                    gold.toLong() + saleValue <= Int.MAX_VALUE.toLong()
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(good.name, fontWeight = FontWeight.Bold)
                                        Text("Buy: ${quote.price} gold • Sell: ${saleQuote.price} gold • ${quote.trendLabel} (${quote.trendPercent}%)")
                                        Text("Event: ${quote.eventLabel}")
                                        Text(
                                            MarketSystem.trendRiskLabel(quote.trendRisk),
                                            color = when (quote.trendRisk) {
                                                "High" -> Color(0xFFB3261E)
                                                "Moderate" -> Color(0xFF9A6700)
                                                else -> Color(0xFF1B5E20)
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("Port stock: ${quote.availableStock} • In cargo: $held")
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                enabled = MarketSystem.canBuy(quote, quantity, gold) && cargoFits,
                                                onClick = {
                                                    val transaction = marketState.buy(
                                                        marketCity.id, good, quote, quantity, gold, cargoCapacity = capacity
                                                    )
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
                                                    val transaction = marketState.sell(marketCity.id, good, saleQuote, quantity, gold)
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
                        }
                        Text("Prices reflect port, year, stock and deterministic supply/demand events. Trading is only available at your current port.")
                    }
                    4 -> {
                        ShopDashboard(
                            cities = cities,
                            selectedCityId = shopCityId,
                            gold = gold,
                            year = year,
                            shopState = shopState,
                            marketState = marketState,
                            onCitySelected = { shopCityId = it },
                            onOpenShop = { cityId, cityOwned ->
                                val transaction = shopState.openShop(cityId, cityOwned, gold, marketState.cargo)
                                if (transaction.success) {
                                    shopState = transaction.state
                                    gold = transaction.gold
                                }
                                message = transaction.message
                            },
                            onStock = { cityId, goodName, amount ->
                                val transaction = shopState.stockFromCargo(cityId, goodName, amount, marketState.cargo, gold)
                                if (transaction.success) {
                                    shopState = transaction.state
                                    marketState = marketState.copy(cargo = transaction.cargo)
                                }
                                message = transaction.message
                            },
                            onCustomerSale = { cityId, goodName, amount, retailPrice ->
                                val transaction = shopState.sellToCustomers(cityId, goodName, amount, retailPrice, gold, marketState.cargo)
                                if (transaction.success) shopState = transaction.state
                                message = transaction.message
                            },
                            onCollectCash = { cityId ->
                                val transaction = shopState.collectCash(cityId, gold, marketState.cargo)
                                if (transaction.success) {
                                    shopState = transaction.state
                                    gold = transaction.gold
                                }
                                message = transaction.message
                            }
                        )
                    }
                    5 -> {
                        Text("ARMY & DEFENSE", fontWeight = FontWeight.Bold)
                        Text("Soldiers: $army • Annual upkeep: ${ArmySystem.annualUpkeep(army)} gold")
                        Text("Upkeep is 1 gold per soldier, paid when you advance the year.")
                        val recruit10Cost = ResearchSystem.recruitCost(100, researchState)
                        val recruit60Cost = ResearchSystem.recruitCost(500, researchState)
                        if (fleetState.isAtSea) Text("Recruiting and attacks are unavailable while your fleet is at sea.")
                        Button(
                            enabled = !fleetState.isAtSea && gold >= recruit10Cost && army <= Int.MAX_VALUE - 10,
                            onClick = {
                                val result = ArmySystem.recruit(army, gold, 10, recruit10Cost)
                                if (result.success) { army = result.army; gold = result.gold }
                                message = result.message
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Recruit 10 • ${recruit10Cost} gold") }
                        Button(
                            enabled = !fleetState.isAtSea && gold >= recruit60Cost && army <= Int.MAX_VALUE - 60,
                            onClick = {
                                val result = ArmySystem.recruit(army, gold, 60, recruit60Cost)
                                if (result.success) { army = result.army; gold = result.gold }
                                message = result.message
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Recruit 60 • ${recruit60Cost} gold") }
                        cities.filter { it.owned }.forEach { ownedCity ->
                            Text("Defense • ${ownedCity.name}: ${ArmySystem.cityDefense(ownedCity)}")
                        }
                        cities.filter { !it.owned }.forEach { target ->
                            val defense = ArmySystem.cityDefense(target)
                            val required = ArmySystem.minimumAttackForce(target)
                            Button(
                                enabled = !fleetState.isAtSea && army >= 10,
                                onClick = {
                                    val battle = ArmySystem.attack(army, target)
                                    if (battle.valid) {
                                        val armyBefore = army
                                        army = battle.remainingArmy
                                        battle.capturedCity?.let { captured ->
                                            cities = cities.map { if (it.id == target.id) captured else it }
                                        }
                                        ArmySystem.playerSiegeReport(selectedCountry, target, armyBefore, battle)?.let { report ->
                                            battleReports = (battleReports + report).takeLast(30)
                                        }
                                    }
                                    message = battle.message
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Attack ${target.name} • Defense $defense • Need $required troops") }
                        }
                    }
                    6 -> {
                        FleetDashboard(
                            cities = cities,
                            fleetState = fleetState,
                            gold = gold,
                            cargo = marketState.cargo,
                            cargoCapacityBonus = ResearchSystem.cargoCapacityBonus(researchState),
                            onPurchase = { shipId ->
                                val action = FleetSystem.purchase(fleetState, shipId, gold)
                                if (action.success) {
                                    fleetState = action.state
                                    gold = action.gold
                                }
                                message = action.message
                            },
                            onSelectShip = { shipId ->
                                val action = FleetSystem.selectShip(fleetState, shipId, gold)
                                if (action.success) fleetState = action.state
                                message = action.message
                            },
                            onDepart = { routeId ->
                                val action = FleetSystem.depart(fleetState, routeId, gold)
                                if (action.success) {
                                    fleetState = action.state
                                    gold = action.gold
                                }
                                message = action.message
                            },
                            onAdvanceDay = {
                                val action = FleetSystem.advanceVoyageDay(fleetState, gold)
                                if (action.success) {
                                    fleetState = action.state
                                    if (!action.state.isAtSea) marketCityId = action.state.currentPortId
                                }
                                message = action.message
                            }
                        )
                    }
                    7 -> {
                        val metrics = kingdomSnapshot.metrics
                        val next = kingdomSnapshot.nextRank
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("KINGDOM PROGRESSION", fontWeight = FontWeight.Bold)
                                Text("Current rank: ${kingdomSnapshot.currentRank.title}", style = MaterialTheme.typography.titleMedium)
                                Text("Settlement founding discount: ${kingdomSnapshot.currentRank.foundingDiscountPercent}%")
                                Text("Current city founding cost: ${cityFoundingCost} gold")
                                Text("Owned cities: ${metrics.ownedCities}")
                                Text("Owned population: ${metrics.ownedPopulation}")
                                Text("Building levels: ${metrics.ownedBuildingLevels}")
                                if (next != null) {
                                    Text("NEXT RANK • ${next.title}", fontWeight = FontWeight.Bold)
                                    Text("Cities: ${metrics.ownedCities} / ${next.ownedCitiesRequired}")
                                    Text("Population: ${metrics.ownedPopulation} / ${next.populationRequired}")
                                    Text("Building levels: ${metrics.ownedBuildingLevels} / ${next.buildingLevelsRequired}")
                                    Text("Reach all three targets to unlock the next founding discount.")
                                } else {
                                    Text("Maximum kingdom rank reached. Your realm is an empire.")
                                }
                            }
                        }
                    }
                    8 -> {
                        ResearchDashboard(
                            state = researchState,
                            gold = gold,
                            ownedPopulation = kingdomSnapshot.metrics.ownedPopulation,
                            onResearch = { techId ->
                                val action = ResearchSystem.research(researchState, techId, gold, kingdomSnapshot.metrics.ownedPopulation)
                                if (action.success) {
                                    researchState = action.state
                                    gold = action.gold
                                }
                                message = action.message
                            }
                        )
                    }
                    9 -> {
                        BattleReportScreen(
                            reports = battleReports,
                            onSelectCity = { cityId ->
                                selectedCityId = cityId
                                tab = 0
                            }
                        )
                    }
                }
                Button(
                    onClick = {
                        if (year == Int.MAX_VALUE) {
                            message = "Maximum year reached."
                        } else {
                            year += 1
                            marketState = marketState.restockYearly()
                            val grownCities = WorldTurnSystem.migrateOwnership(cities.map { city ->
                                if (city.owned && (city.controlledBy == null || city.controlledBy == WorldTurnSystem.PLAYER_FACTION)) city.copy(
                                    population = (city.population.toLong() + CityDashboardRules.annualGrowth(city).toLong())
                                        .coerceAtMost(CityGrowthSystem.populationCapacity(city).toLong())
                                        .coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                                ) else city
                            })
                            val enemyTurn = WorldTurnSystem.enemyTurn(enemyKingdomState, grownCities, year, selectedCountry)
                            enemyKingdomState = enemyTurn.state
                            cities = enemyTurn.cities
                            battleReports = (battleReports + enemyTurn.reports).takeLast(30)
                            val income = WorldTurnSystem.kingdomIncome(enemyTurn.cities)
                            val annualSettlement = ArmySystem.settleYear(army, gold, income.toLong())
                            gold = annualSettlement.gold
                            army = annualSettlement.army
                            val enemySummary = enemyTurn.reports.joinToString(" ") { it.summary }
                            message = if (enemySummary.isBlank()) annualSettlement.message else "${annualSettlement.message} $enemySummary"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Advance Year") }
            }
        }
    }
}
