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
    var port by remember { mutableStateOf(ports.first()) }
    var gold by remember { mutableIntStateOf(1000) }
    var cargo by remember { mutableStateOf(mapOf<String, Int>()) }
    var shopOwned by remember { mutableStateOf(false) }
    var stock by remember { mutableStateOf(mapOf<String, Int>()) }
    var capacity by remember { mutableIntStateOf(30) }
    var message by remember { mutableStateOf("Welcome, Merchant! Buy goods, sail to a new port, and trade for profit.") }
    var qty by remember { mutableIntStateOf(1) }
    val cargoTotal = cargo.values.sum()

    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2D5743), secondary = Color(0xFFC58B43), background = Color(0xFFF3EBDD))) {
        Column(Modifier.fillMaxSize().background(Color(0xFFF3EBDD))) {
            Column(Modifier.fillMaxWidth().background(Color(0xFF1E342B)).padding(14.dp)) {
                Text("⚓ OLD WORLD TRADER", fontSize = 23.sp, color = Color(0xFFF4D49A), fontWeight = FontWeight.Black)
                Text("A merchant's journey across ancient seas", color = Color(0xFFD8E3D9), fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    StatPill("💰 Gold", gold.toString())
                    StatPill("📦 Cargo", "$cargoTotal / $capacity")
                    StatPill("🏪 Shop", if (shopOwned) "Owned" else "Locked")
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
                            Text("Price: $price gold • Cargo: $owned", fontSize = 12.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(onClick = { qty = (qty - 1).coerceAtLeast(1) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(32.dp)) { Text("−") }
                                Text(" $qty ", fontWeight = FontWeight.Bold)
                                Button(onClick = { qty = (qty + 1).coerceAtMost(10) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(32.dp)) { Text("+") }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Button(onClick = {
                                    if (gold >= price * qty && cargoTotal + qty <= capacity) {
                                        gold -= price * qty
                                        cargo = cargo + (good.name to ((cargo[good.name] ?: 0) + qty))
                                        message = "Bought $qty ${good.name} in ${port.name}."
                                    } else message = if (gold < price * qty) "Not enough gold!" else "Ship is full!"
                                }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Buy", fontSize = 12.sp) }
                                Button(onClick = {
                                    if (owned >= qty) {
                                        gold += price * qty
                                        cargo = cargo + (good.name to (owned - qty))
                                        message = "Sold $qty ${good.name} for $price each."
                                    } else message = "Not enough ${good.name} to sell."
                                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B6332)), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Sell", fontSize = 12.sp) }
                            }
                        }
                    }
                }
                item { SectionTitle("⛵ Sail to another city") }
                items(ports.filter { it.name != port.name }) { destination ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE7DDCA), shape = RoundedCornerShape(12.dp))) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${destination.emoji} ${destination.name}\n${destination.subtitle}", Modifier.weight(1f), color = Color(0xFF344438))
                            Button(onClick = { port = destination; message = "Arrived at ${destination.name}! Market prices changed." }) { Text("Sail") }
                        }
                    }
                }
                item { SectionTitle("🏪 Merchant Shop") }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFDFD0B5), shape = RoundedCornerShape(14.dp))) {
                        Column(Modifier.padding(14.dp)) {
                            Text(if (shopOwned) "Shop Owner: ${port.name}" else "Buy your own shop", fontWeight = FontWeight.Bold, color = Color(0xFF283B2F))
                            Text(if (shopOwned) "Stock your shop using goods from your cargo." else "Purchase a shop for 500 gold.", color = Color(0xFF465347), fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            if (!shopOwned) Button(onClick = {
                                if (gold >= 500) { gold -= 500; shopOwned = true; message = "You now own a shop in ${port.name}!" }
                                else message = "You need 500 gold."
                            }) { Text("Purchase Shop — 500 gold") }
                            else goods.forEach { good ->
                                val inCargo = cargo[good.name] ?: 0
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${good.emoji} ${good.name}: $inCargo", Modifier.weight(1f), fontSize = 12.sp)
                                    Button(enabled = inCargo > 0, onClick = {
                                        cargo = cargo + (good.name to (inCargo - 1))
                                        stock = stock + (good.name to ((stock[good.name] ?: 0) + 1))
                                        message = "Added one ${good.name} to shop stock."
                                    }) { Text("Stock 1") }
                                }
                                Text("Shop stock: ${stock[good.name] ?: 0}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(14.dp)); Text("Prototype • Offline gameplay • No internet or account required", fontSize = 11.sp, color = Color.Gray) }
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
