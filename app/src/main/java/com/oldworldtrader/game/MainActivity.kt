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
class MainActivity:ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{Game()}} }
private data class Good(val name:String,val price:Int)
private data class City(val name:String,val value:Int,val defense:Int)
private data class Ship(val name:String,val cost:Int,val capacity:Int)
@Composable private fun Game(){
 val goods=listOf(Good("Spices",90),Good("Silk",150),Good("Iron",70),Good("Wood",45),Good("Wheat",30))
 val cities=listOf(City("Alexandria",6000,20),City("Venice",12000,40),City("Muscat",4500,15),City("Delhi",8000,25),City("Baghdad",9000,30))
 val ships=listOf(Ship("Small Boat",500,20),Ship("Merchant Ship",2500,60),Ship("Cargo Ship",8000,150))
 var tab by remember{mutableIntStateOf(0)}
 var gold by remember{mutableIntStateOf(1000)}
 var year by remember{mutableIntStateOf(1)}
 var city by remember{mutableStateOf(cities[0])}
 var cargo by remember{mutableStateOf(mapOf<String,Int>())}
 var fleet by remember{mutableStateOf(listOf(ships[0]))}
 var bonus by remember{mutableIntStateOf(0)}
 var owned by remember{mutableStateOf(setOf<String>())}
 var qty by remember{mutableIntStateOf(1)}
 var msg by remember{mutableStateOf("Trade goods and grow your empire.")}
 val cap=fleet.last().capacity+bonus
 val tabs=listOf("Shop","Cities","Ships","Market","Army")
 MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF285743))){
 Column(Modifier.fillMaxSize().background(Color(0xFFF3EBDD))){
 Column(Modifier.fillMaxWidth().background(Color(0xFF1E342B)).padding(12.dp)){
 Text("⚓ OLD WORLD TRADER",color=Color(0xFFF2D49A),fontWeight=FontWeight.Bold)
 Text("Gold: $gold | Cargo: ${cargo.values.sum()}/$cap | Ships: ${fleet.size} | Year: $year",color=Color.White)
 }
 ScrollableTabRow(selectedTabIndex=tab){tabs.forEachIndexed{i,label->Tab(selected=tab==i,onClick={tab=i},text={Text(label)})}}
 Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(10.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
 Text(msg)
 when(tab){
 0->{Text("SHOP",fontWeight=FontWeight.Bold);Button(enabled=gold>=500,onClick={gold-=500;msg="Shop purchased"},modifier=Modifier.fillMaxWidth()){Text("Buy shop • 500")};goods.forEach{g->Button(enabled=(cargo[g.name]?:0)>0,onClick={cargo=cargo+(g.name to ((cargo[g.name]?:0)-1));msg="Stocked ${g.name}"},modifier=Modifier.fillMaxWidth()){Text("Stock ${g.name}")}}}
 1->{Text("CITY ASSETS",fontWeight=FontWeight.Bold);cities.forEach{c->Column(Modifier.fillMaxWidth().padding(6.dp)){Text("${c.name} • value ${c.value} • defense ${c.defense}");Button(onClick={city=c;msg="Travelled to ${c.name}"},modifier=Modifier.fillMaxWidth()){Text("Travel")};Button(enabled=c.name !in owned&&gold>=c.value,onClick={gold-=c.value;owned=owned+c.name;msg="Bought ${c.name}"},modifier=Modifier.fillMaxWidth()){Text("Buy city ${c.value}")};Button(enabled=c.name in owned,onClick={gold+=c.value*80/100;owned=owned-c.name},modifier=Modifier.fillMaxWidth()){Text("Sell city")}};Text("Owned cities: ${owned.joinToString()}")}
 2->{Text("SHIPYARD",fontWeight=FontWeight.Bold);Text("Fleet capacity ${fleet.sumOf{it.capacity}+bonus}");Button(enabled=gold>=800,onClick={gold-=800;bonus+=25},modifier=Modifier.fillMaxWidth()){Text("Capacity upgrade • 800")};ships.forEach{s->Button(enabled=gold>=s.cost,onClick={gold-=s.cost;fleet=fleet+s;msg="Purchased ${s.name}"},modifier=Modifier.fillMaxWidth()){Text("Buy ${s.name} • ${s.cost}")}}}
 3->{Text("MARKET • ${city.name}",fontWeight=FontWeight.Bold);Row{Button(onClick={qty=(qty-1).coerceAtLeast(1)}){Text("-")};Text("Qty $qty",Modifier.padding(12.dp));Button(onClick={qty=(qty+1).coerceAtMost(99)}){Text("+")}};goods.forEach{g->Text("${g.name} • price ${g.price} • cargo ${cargo[g.name]?:0}");Row{Button(enabled=gold>=g.price*qty&&cargo.values.sum()+qty<=cap,onClick={gold-=g.price*qty;cargo=cargo+(g.name to ((cargo[g.name]?:0)+qty))},modifier=Modifier.weight(1f)){Text("Buy")};Button(enabled=(cargo[g.name]?:0)>=qty,onClick={gold+=g.price*qty;cargo=cargo+(g.name to ((cargo[g.name]?:0)-qty))},modifier=Modifier.weight(1f)){Text("Sell")}}}}
 4->{Text("ARMY / CONQUEST",fontWeight=FontWeight.Bold);Text("Army power: ${owned.size*20}");cities.filter{it.name!=city.name&&it.name !in owned}.forEach{c->Button(enabled=owned.size*20>=c.defense,onClick={owned=owned+c.name;msg="Conquered ${c.name}"},modifier=Modifier.fillMaxWidth()){Text("Conquer ${c.name} • defense ${c.defense}")}}}
 }
 Text("Annual income estimate: ${owned.size*300} gold")
 Button(onClick={gold+=owned.size*300;year++;msg="Year advanced; income collected"},modifier=Modifier.fillMaxWidth()){Text("Advance year")}
 }
 }
 }
}