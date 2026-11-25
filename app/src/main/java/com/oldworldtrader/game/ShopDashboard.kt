package com.oldworldtrader.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ShopDashboard(
    cities: List<CityData>,
    selectedCityId: String,
    gold: Int,
    year: Int,
    shopState: ShopState,
    marketState: MarketState,
    onCitySelected: (String) -> Unit,
    onOpenShop: (String, Boolean) -> Unit,
    onStock: (String, String, Int) -> Unit,
    onCustomerSale: (String, String, Int, Int) -> Unit,
    onCollectCash: (String) -> Unit
) {
    var cityMenuExpanded by remember { mutableStateOf(false) }
    val city = cities.firstOrNull { it.id == selectedCityId } ?: cities.firstOrNull()
    if (city == null) {
        Text("Found or conquer a city to manage a shop.")
        return
    }
    val cityIndex = cities.indexOf(city).coerceAtLeast(0)
    val shop = shopState.shop(city.id)

    Text("CITY SHOP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    OutlinedButton(onClick = { cityMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
        Text("City: ${city.name} • ${city.country} ▼")
    }
    DropdownMenu(expanded = cityMenuExpanded, onDismissRequest = { cityMenuExpanded = false }) {
        cities.forEach { option ->
            DropdownMenuItem(
                text = { Text("${option.name} • ${option.country}") },
                onClick = {
                    onCitySelected(option.id)
                    cityMenuExpanded = false
                }
            )
        }
    }

    if (shop == null) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("NO SHOP IN ${city.name.uppercase()}", fontWeight = FontWeight.Bold)
                Text("Build a local retail shop to sell goods to residents. It costs ${ShopSystem.BUILD_COST} gold and requires city ownership.")
                if (!city.owned) Text("You must own this city first.", color = MaterialTheme.colorScheme.error)
                Button(
                    enabled = city.owned && gold >= ShopSystem.BUILD_COST,
                    onClick = { onOpenShop(city.id, city.owned) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Build Shop • ${ShopSystem.BUILD_COST} gold") }
            }
        }
    } else {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("YOUR SHOP • ${city.name}", fontWeight = FontWeight.Bold)
                Text("Shop cash: ${shop.cash} gold")
                Text("Lifetime revenue: ${shop.lifetimeRevenue} gold")
                Text("Items sold: ${shop.itemsSold}")
                Button(
                    enabled = shop.cash > 0 && gold >= 0 && gold.toLong() + shop.cash.toLong() <= Int.MAX_VALUE.toLong(),
                    onClick = { onCollectCash(city.id) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Collect Shop Cash • ${shop.cash} gold") }
            }
        }

        Text("INVENTORY • STOCK GOODS FROM YOUR CARGO", fontWeight = FontWeight.Bold)
        EconomyLogic.goods.forEach { good ->
            val shopStock = shopState.stock(city.id, good.name)
            val cargo = marketState.cargoAmount(good.name)
            val factors = MarketSystem.factorsFor(good, cityIndex, year)
            val previous = MarketSystem.factorsFor(good, cityIndex, (year - 1).coerceAtLeast(1))
            val quote = MarketSystem.quote(
                good = good,
                cityIndex = cityIndex,
                year = year,
                stock = marketState.stock(city.id, good.name),
                citySupplyFactor = factors.supplyFactor,
                demandFactor = factors.demandFactor,
                previousCitySupplyFactor = previous.supplyFactor,
                previousDemandFactor = previous.demandFactor,
                eventLabel = factors.label
            )
            val retailPrice = ShopSystem.retailPrice(quote.price)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(good.name, fontWeight = FontWeight.Bold)
                    Text("Market price: ${quote.price} • Customer price: ${retailPrice} gold")
                    Text("Shop stock: ${shopStock} • Player cargo: ${cargo}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            enabled = cargo > 0,
                            onClick = { onStock(city.id, good.name, 1) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Stock 1") }
                        OutlinedButton(
                            enabled = shopStock > 0,
                            onClick = { onCustomerSale(city.id, good.name, 1, retailPrice) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Sell 1") }
                    }
                }
            }
        }
        Text("Customer sales add to shop cash; use Collect Shop Cash to move it into your wallet.")
    }
}
