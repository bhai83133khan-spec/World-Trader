package com.oldworldtrader.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OldWorldTraderApp() }
    }
}
private data class Good(val name: String, val emoji: String, val base: Int)
private data class Port(val name: String, val subtitle: String, val emoji: String, val factor: Float)
private data class ShipType(val name: String, val emoji: String, val price: Int, val capacity: Int)
private data class Ship(val type: ShipType, val level: Int = 0) {
    val capacity: Int get() = type.capacity + level * 25
  val displayName: String get() = if (level == 0) type.name else "${type.name} Lv.${level + 1}"
}

@Composable
private fun OldWorldTraderApp() {
    val ports = listOf(
        Port("Alexandria", "Spice & cloth port", "🏛️", 1.12f),
        Port("Constantinople", "Silk road gateway", "🕌", 1.28f),
        Port("Muscat", "Pearls & incense", "🌴", .88f),
        Port("Venice", "Luxury trade hub", "⛵", 1.35f),
        Port("Delhi", "Metal & textile market", "🏰", .96f)
    )
    val goods = listOf(
        Good("Spices", "🌶️", 90), Good("Silk", "🧵", 150), Good("Iron", "⚒️", 70),
        Good("Wood", "🪵", 45), Good("Wheat", "🌾", 30), Good("Pearls", "🫧", 220)
    )
    val shipTypes = listOf(
        ShipType("Small Boat", "🛶", 500, 20),
        ShipType("Merchant Ship", "⛵", 2500, 60),
        ShipType("Cargo Ship", "🚢", 8000, 150),
        ShipType("Large Trading Vessel", "⚓", 20000, 350)
    )
    var port by remember { mutableStateOf(ports.first()) }
    var gold by remember { mutableIntStateOf(1000) }
    var cargo by remember { mutableStateOf(mapOf<String, Int>()) }
    var shopOwned by remember { mutableStateOf(false) }
    var shopLevel by remember { mutableIntStateOf(1) }
    var stock by remember { mutableStateOf(mapOf<String, Int>()) }
    var ships by remember { mutableStateOf(listOf(Ship(shipTypes[0]))) }
    var activeShipIndex by remember { mutableIntStateOf(0) }
    var age by remember { mutableIntStateOf(18) }
    var year by remember { mutableIntStateOf(1) }
    var homeLevel by remember { mutableIntStateOf(0) }
    var married by remember { mutableStateOf(false) }
    var children by remember { mutableIntStateOf(0) }
    var qty by remember { mutableIntStateOf(1) }
    var message by remember { mutableStateOf("Welcome, Merchant! Your journey begins with 1,000 gold and a small boat.") }
    val activeShip = ships.getOrNull(activeShipIndex) ?: ships.first()
    val cargoTotal = cargo.values.sum()
    val capacity = activeShip.capacity
    val homeNames = listOf("No Home", "Room Rental", "Small House", "Merchant Villa", "Grand Estate")
    val homePrices = listOf(0, 800, 3000, 10000, 35000)
    val homeCosts = listOf(0, 25, 60, 150, 350)

    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2D5743), secondary = Color(0xFFC58B43), background = Color(0xFFF3EBDD))) {
        Column(Modifier.fillMaxSize().background(Color(0xFFF3EBDD))) {
            Column(Modifier.fillMaxWidth().background(Color(0xFF1E342B)).padding(14.dp)) {
                Text("⚓ OLD WORLD TRADER", fontSize = 23.sp, color = Color(0xFFF4D49A), fontWeight = FontWeight.Black)
                Text("Build a merchant dynasty across the ancient world", color = Color(0xFFD8E3D9), fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatPill("💰 Gold", gold.toString())
                    StatPill("📦 Cargo", "$cargoTotal/$capacity")
                    StatPill("🧑 Age", "$age")
                    StatPill("⛵ Fleet", ships.size.toString())
                }
            }
            LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxSize()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFDFD0B5)), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Text("📍 CURRENT PORT", fontWeight = FontWeight.Bold, color = Color(0xFF435D4A))
                            Text("${port.emoji} ${port.name}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263A2D))
                            Text(port.subtitle, color = Color(0xFF5B665B))
                            Spacer(Modifier.height(8.dp))
                            Text(message, color = Color(0xFF35453A), fontSize = 13.sp)
                        }
                    }
                }
                item { SectionTitle("🛒 Market — Buy & Sell") }
                items(goods) { good ->
                    val price = (good.base * port.factor).toInt()
                    val owned = cargo[good.name] ?: 0
                    Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${good.emoji} ${good.name}", fontWeight = FontWeight.Bold, color = Color(0xFF283B2F))
                            Text("Price: $price gold • In cargo: $owned", fontSize = 12.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(onClick = { qty = (qty - 1).coerceAtLeast(1) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(32.dp)) { Text("−") }
                                Text(" $qty ", fontWeight = FontWeight.Bold)
                                Button(onClick = { qty = (qty + 1).coerceAtMost(100) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(32.dp)) { Text("+") }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Button(onClick = {
                                    if (gold >= price * qty && cargoTotal + qty <= capacity) {
                                        gold -= price * qty
                                        cargo = cargo + (good.name to ((cargo[good.name] ?: 0) + qty))
                                        message = "Purchased $qty ${good.name} in ${port.name}."
                                    } else message = if (gold < price * qty) "Not enough gold!" else "Cargo full. Upgrade or change ship."
                                }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Buy", fontSize = 12.sp) }
                                Button(onClick = {
                                    if (owned >= qty) {
                                        gold += price * qty
                                        cargo = cargo + (good.name to (owned - qty))
                                        message = "Sold $qty ${good.name} at ${port.name} for ${price * qty} gold."
                                    } else message = "Not enough ${good.name} to sell."
                                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6332)), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Sell", fontSize = 12.sp) }
                            }
                        }
                    }
                }
                item { SectionTitle("⛵ Travel") }
                items(ports.filter { it.name != port.name }) { destination ->
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE7DDCA)) ) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${destination.emoji} ${destination.name}\n${destination.subtitle}", Modifier.weight(1f), color = Color(0xFF344438))
                            Button(onClick = { port = destination; message = "Docked at ${destination.name}. Local prices updated." }) { Text("Sail") }
                        }
                    }
                }
                item { SectionTitle("🚢 Shipyard — Fleet Management") }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFDFD0B5)), shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Active ship: ${activeShip.type.emoji} ${activeShip.displayName} • Cargo capacity ${activeShip.capacity}", fontWeight = FontWeight.Bold, color = Color(0xFF283B2F))
                            Text("Ships owned: ${ships.size}", color = Color.DarkGray)
                            ships.forEachIndexed { index, ship ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${ship.type.emoji} ${ship.displayName} (${ship.capacity})", Modifier.weight(1f), fontSize = 12.sp)
                                    if (index == activeShipIndex) Text("ACTIVE", color = Color(0xFF2D5743), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    else TextButton(onClick = { activeShipIndex = index; message = "Selected ${ship.displayName}." }) { Text("Use") }
                                    TextButton(onClick = {
                                        if (ships.size > 1 && index != activeShipIndex && cargoTotal <= ships.filterIndexed { ix, _ -> ix != index }.maxOfOrNull { it.capacity } ?: 0) {
                                            gold += ship.type.price / 2
                                            ships = ships.filterIndexed { ix, _ -> ix != index }
                                            if (index < activeShipIndex) activeShipIndex--
                                            message = "Sold ${ship.type.name} for ${ship.type.price / 2} gold."
                                        } else message = "Cannot sell your active ship or a ship that would leave cargo without capacity."
                                    }) { Text("Sell") }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            shipTypes.forEach { type ->
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text("${type.emoji} ${type.name}", fontWeight = FontWeight.SemiBold)
                                        Text("${type.price} gold • Capacity ${type.capacity}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Button(enabled = gold >= type.price, onClick = {
                                        gold -= type.price
                                        ships = ships + Ship(type)
                                        activeShipIndex = ships.lastIndex
                                        message = "Purchased ${type.name}. New ship selected!"
                                    }) { Text("Buy") }
                                }
                            }
                            Button(enabled = gold >= 600 && activeShip.level < 3, onClick = {
                                gold -= 600
                                ships = ships.toMutableList().also { it[activeShipIndex] = activeShip.copy(level = activeShip.level + 1) }
                                message = "Ship upgraded! +25 cargo capacity."
                            }) { Text("Upgrade Active Ship — 600 gold (+25 cargo)") }
                        }
                    }
                }
                item { SectionTitle("🏪 Merchant Business") }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFDFD0B5)), shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(if (shopOwned) "Shop Level $shopLevel • ${port.name}" else "Own a Merchant Shop", fontWeight = FontWeight.Bold, color = Color(0xFF283B2F))
                            Text(if (shopOwned) "Stock goods from your cargo. Upgrade shop for larger business." else "Buy your first shop for 500 gold.", color = Color(0xFF465347), fontSize = 13.sp)
                            if (!shopOwned) Button(onClick = {
                                if (gold >= 500) { gold -= 500; shopOwned = true; message = "Shop purchased in ${port.name}!" } else message = "Need 500 gold."
                            }) { Text("Buy Shop — 500 gold") }
                            if (shopOwned) {
                                goods.forEach { good ->
                                    val inCargo = cargo[good.name] ?: 0
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("${good.emoji} ${good.name}: cargo $inCargo • shop ${stock[good.name] ?: 0}", Modifier.weight(1f), fontSize = 12.sp)
                                        Button(enabled = inCargo > 0, onClick = {
                                            cargo = cargo + (good.name to (inCargo - 1))
                                            stock = stock + (good.name to ((stock[good.name] ?: 0) + 1))
                                            message = "Stocked one ${good.name} in shop."
                                        }) { Text("Stock 1") }
                                    }
                                }
                                Button(enabled = gold >= 1500, onClick = { gold -= 1500; shopLevel++; message = "Shop expanded to level $shopLevel!" }) { Text("Expand Shop — 1,500 gold") }
                            }
                        }
                    }
                }
                item { SectionTitle("🏡 Home & Lifestyle") }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Current home: ${homeNames[homeLevel]} • Monthly cost ${homeCosts[homeLevel]} gold", fontWeight = FontWeight.Bold, color = Color(0xFF283B2F))
                            if (homeLevel < homeNames.lastIndex) {
                                Button(enabled = gold >= homePrices[homeLevel + 1], onClick = {
                                    gold -= homePrices[homeLevel + 1]
                                    homeLevel++
                                    message = "Upgraded residence to ${homeNames[homeLevel]}."
                                }) { Text("Upgrade to ${homeNames[homeLevel + 1]} — ${homePrices[homeLevel + 1]} gold") }
                            }
                            Text("Family: ${if (married) "Married" else "Single"} • Children: $children")
                            if (!married) Button(enabled = age >= 21 && gold >= 1000, onClick = { gold -= 1000; married = true; message = "Congratulations! You married and started a family." }) { Text("Marry — 1,000 gold (age 21+)") }
                            if (married && children < 5) Button(enabled = gold >= 500, onClick = { gold -= 500; children++; message = "A new child joins your family. Your legacy grows!" }) { Text("Welcome a child — 500 gold") }
                        }
                    }
                }
                item { SectionTitle("📅 Yearly Progression") }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE7DDCA)), shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Year $year • Age $age", fontWeight = FontWeight.Bold, color = Color(0xFF283B2F))
                            Text("Advance one year. Pay household costs, then grow older. Expenses depend on home and family.", fontSize = 12.sp, color = Color.Gray)
                            Button(onClick = {
                                val familyCost = if (married) 100 + children * 60 else 0
                                val expenses = (homeCosts[homeLevel] + familyCost) * 12
                                gold -= expenses
                                age++
                                year++
                                message = "Year advanced. Household expenses paid: $expenses gold. ${if (gold < 0) "You are in debt—earn gold to recover!" else "Keep trading to grow your fortune."}"
                            }) { Text("Advance 1 Year (pay expenses)") }
                        }
                    }
                }
                item { Spacer(Modifier.height(14.dp)); Text("Offline prototype • Local gameplay • No accounts or ads", fontSize = 11.sp, color = Color.Gray) }
            }
        }
    }
}
@Composable private fun StatPill(label: String, value: String) {
    Column(Modifier.background(Color(0xFF355343), RoundedCornerShape(10.dp)).padding(horizontal = 9.dp, vertical = 7.dp)) {
        Text(label, fontSize = 10.sp, color = Color(0xFFDBE5DC))
        Text(value, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
    }
}
@Composable private fun SectionTitle(text: String) { Text(text, Modifier.padding(top = 4.dp, bottom = 2.dp), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF293D30)) }
