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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{GameScreen()}}}
private data class Good(val name:String,val base:Int)
private data class City(val name:String,val value:Int,val defense:Int)
private data class Ship(val name:String,val cost:Int,val capacity:Int)
@Composable private fun GameScreen(){
 val goods=listOf(Good("Spices",90),Good("Silk",150),Good("Iron",70),Good("Wood",45),Good("Wheat",30))
 val cities=listOf(City("Alexandria",6000,20),City("Venice",12000,40),City("Muscat",4500,15),City("Delhi",8000,25),City("Baghdad",9000,30))
 val ships=listOf(Ship("Small Boat",500,20),Ship("Merchant Ship",2500,60),Ship("Cargo Ship",8000,150))
 var tab by remember{mutableIntStateOf(0)};var gold by remember{mutableIntStateOf(1000)};var year by remember{mutableIntStateOf(1)}
 var currentCity by remember{mutableStateOf(cities[0])};var cargo by remember{mutableStateOf(mapOf<String,Int>())};var shopStock by remember{mutableStateOf(mapOf<String,Int>())}
 var fleet by remember{mutableStateOf(listOf(ships[0]))};var bonus by remember{mutableIntStateOf(0)};var owned by remember{mutableStateOf(setOf<String>())}
 var qty by remember{mutableIntOf(2)};var message by remember{mutableStateOf("Trade goods, stock your shop, and watch local prices.")}
 val capacity=fleet.sumOf{it.capacity}+bonus;val tabs=listOf("Shop","Cities","Ships","Market","Army")
 fun cityIndex()=cities.indexOf(currentCity).coerceAtLeast(0)
 fun price(g:Good,c:City):Int { val ci=cities.indexOf(c).coerceAtLeast(0);val gi=goods.indexOf(g).coerceAtLeast(0);val cityFactor=90+ci*13+((gi+ci)%3)*7;val trend=100+(year-1)*5;return (g.base*cityFactor*trend/10000).coerceAtLeast(1) }
 MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF285743))){
  Column(Modifier.fillMaxSize().background(Color(0xFFF3EBDD))){
   Column(Modifier.fillMaxWidth().background(Color(0xFF1E342B)).padding(12.dp)){Text("⚓ OLD WORLD TRADER",color=Color(0xFFF2D49A),fontWeight=FontWeight.Bold);Text("Gold: $gold | Cargo: ${cargo.values.sum()}/$capacity | Fleet: ${fleet.size} | Year: $year",color=Color.White)}
   ScrollableTabRow(selectedTabIndex=tab){tabs.forEachIndexed{i,l->Tab(selected=tab==i,onClick={tab=i},text={Text(l)})}}
   Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(10.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
    Text(message)
    when(tab){
     0->{Text("SHOP STOCKROOM",fontWeight=FontWeight.Bold);Text("Qty to stock: $qty");Row{Button(onClick={qty=(qty-1).coerceAtLeast(1)}){Text("−")};Text("$qty",Modifier.padding(12.dp));Button(onClick={qty=(qty+1).coerceAtMost(99)}){Text("+")}}
      goods.forEach{g->Column(Modifier.fillMaxWidth().padding(4.dp)){Text("${g.name} | Cargo: ${cargo[g.name]?:0} | Shop stock: ${shopStock[g.name]?:0}");Button(enabled=(cargo[g.name]?:0)>=qty,onClick={cargo=cargo+(g.name to ((cargo[g.name]?:0)-qty));shopStock=shopStock+(g.name to ((shopStock[g.name]?:0)+qty));message="Added $qty ${g.name} to shop stock"},modifier=Modifier.fillMaxWidth()){Text("STOCK IN SHOP • $qty")}}}
     1->{Text("CITY ASSETS",fontWeight=FontWeight.Bold);cities.forEach{c->Column(Modifier.fillMaxWidth().padding(6.dp)){Text("${c.name} • value ${c.value} • defense ${c.defense}");Button(onClick={currentCity=c;message="Travelled to ${c.name}"},modifier=Modifier.fillMaxWidth()){Text("Travel to ${c.name}")};Button(enabled=c.name !in owned&&gold>=c.value,onClick={gold-=c.value;owned=owned+c.name;message="Bought ${c.name}"},modifier=Modifier.fillMaxWidth()){Text("Buy city • ${c.value}")};Button(enabled=c.name in owned,onClick={gold+=c.value*80/100;owned=owned-c.name},modifier=Modifier.fillMaxWidth()){Text("Sell city")}}};Text("Owned: ${owned.joinToString()}")}
     2->{Text("SHIPYARD",fontWeight=FontWeight.Bold);Text("Fleet capacity: $capacity");Button(enabled=gold>=800,onClick={gold-=800;bonus+=25},modifier=Modifier.fillMaxWidth()){Text("Capacity upgrade • 800")};ships.forEach{s->Button(enabled=gold>=s.cost,onClick={gold-=s.cost;fleet=fleet+s;message="Purchased ${s.name}"},modifier=Modifier.fillMaxWidth()){Text("Buy ${s.name} • ${s.cost}")}}}
     3->{Text("MARKET • ${currentCity.name}",fontWeight=FontWeight.Bold);Text("City-specific prices • rising trend shown ↑");Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Button(onClick={qty=(qty-1).coerceAtLeast(1)}){Text("−")};Text("Buy/Sell quantity: $qty",Modifier.padding(8.dp));Button(onClick={qty=(qty+1).coerceAtMost(99)}){Text("+")}}
      goods.forEach{g->val p=price(g,currentCity);val trend="↑ Rising";Column(Modifier.fillMaxWidth().padding(4.dp)){Text("${g.name} • ${p} gold",fontWeight=FontWeight.Bold);Text("Price trend: $trend  |  Cargo: ${cargo[g.name]?:0}  |  Shop stock: ${shopStock[g.name]?:0}");Row{Button(enabled=gold>=p*qty&&cargo.values.sum()+qty<=capacity,onClick={gold-=p*qty;cargo=cargo+(g.name to ((cargo[g.name]?:0)+qty));message="Bought $qty ${g.name} in ${currentCity.name}"},modifier=Modifier.weight(1f)){Text("Buy")};Button(enabled=(cargo[g.name]?:0)>=qty,onClick={gold+=p*qty;cargo=cargo+(g.name to ((cargo[g.name]?:0)-qty));message="Sold $qty ${g.name} in ${currentCity.name}"},modifier=Modifier.weight(1f)){Text("Sell")}}}}
     4->{Text("ARMY / CONQUEST",fontWeight=FontWeight.Bold);Text("Army power: ${owned.size*20}");cities.filter{it.name!=currentCity.name&&it.name !in owned}.forEach{target->Button(enabled=owned.size*20>=target.defense,onClick={owned=owned+target.name;message="Conquered ${target.name}"},modifier=Modifier.fillMaxWidth()){Text("Conquer ${target.name} • defense ${target.defense}")}}}
    }
    Text("Annual income estimate: ${owned.size*300} gold");Button(onClick={gold+=owned.size*300;year++;message="Year advanced. Prices increased and income collected."},modifier=Modifier.fillMaxWidth()){Text("Advance year")}
   }
  }
 }
}