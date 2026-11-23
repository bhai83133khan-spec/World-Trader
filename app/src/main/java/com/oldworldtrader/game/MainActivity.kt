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
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{Screen()}}}
private data class Item(val name:String,val price:Int)
private data class City(val name:String,val country:String,val value:Int,val defense:Int)
private data class Boat(val name:String,val cost:Int,val capacity:Int)
@Composable private fun Screen(){
 val items=listOf(Item("Spices",90),Item("Silk",150),Item("Iron",70),Item("Wood",45),Item("Wheat",30),Item("Pearls",220))
 val cities=listOf(City("Alexandria","Egypt",6000,20),City("Constantinople","Byzantine Empire",10000,35),City("Muscat","Oman",4500,15),City("Venice","Venetian Republic",12000,40),City("Delhi","Delhi Sultanate",8000,25),City("Baghdad","Abbasid Caliphate",9000,30),City("Damascus","Levant",7500,28),City("Samarkand","Central Asia",7000,32))
 val boats=listOf(Boat("Small boat",500,20),Boat("Merchant ship",2500,60),Boat("Cargo ship",8000,150),Boat("Large vessel",20000,350))
 var tab by remember{mutableIntStateOf(0)};var gold by remember{mutableIntStateOf(1000)};var year by remember{mutableIntStateOf(1)};var city by remember{mutableStateOf(cities[0])}
 var cargo by remember{mutableStateOf(mapOf<String,Int>())};var fleet by remember{mutableStateOf(listOf(boats[0]))};var active by remember{mutableIntStateOf(0)};var bonus by remember{mutableIntStateOf(0)};var qty by remember{mutableIntStateOf(1)}
 var owned by remember{mutableStateOf(setOf<String>())};var countries by remember{mutableStateOf(setOf<String>())};var army by remember{mutableStateOf(mapOf<String,Int>())};var note by remember{mutableStateOf("Welcome trader!")}
 val capacity=fleet[active.coerceIn(0,fleet.lastIndex)].capacity+bonus;val total=cargo.values.sum();val tabs=listOf("Shop","Cities","Ships","Market","Army");val power=army.values.sum()*20;val income=owned.sumOf{n->(cities.find{it.name==n}?.value?:0)/20}+countries.size*500
 MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF285743))){Column(Modifier.fillMaxSize().background(Color(0xFFF3EBDD))){
 Column(Modifier.fillMaxWidth().background(Color(0xFF1E342B)).padding(12.dp)){Text("OLD WORLD TRADER",color=Color(0xFFF2D49A),fontWeight=FontWeight.Bold);Text("Gold "+gold+" • Cargo "+total+"/"+capacity+" • Ships "+fleet.size+" • Year "+year,color=Color.White)}
 ScrollableTabRow(selectedTabIndex=tab){tabs.forEachIndexed{i,t->Tab(selected=tab==i,onClick={tab=i},text={Text(t)})}}
 Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(10.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Card{Column(Modifier.padding(10.dp)){Text("Current city: "+city.name+" / "+city.country,fontWeight=FontWeight.Bold);Text(note)}}
 when(tab){
 0->{Title("Merchant shop");Action("Buy Shop • 500",gold>=500){gold-=500;note="Shop opened."};items.forEach{g->Action("Stock "+g.name+" from cargo",(cargo[g.name]?:0)>0){cargo=cargo+(g.name to ((cargo[g.name]?:0)-1));note="Stocked "+g.name}}}
 1->{Title("Cities / Country assets");cities.forEach{c->Card{Column(Modifier.padding(8.dp)){Text(c.name+" — "+c.country,fontWeight=FontWeight.Bold);Text("Value "+c.value+" • Defense "+c.defense);Action("Travel",true){city=c};Action("Buy city • "+c.value,c.name !in owned&&gold>=c.value){gold-=c.value;owned=owned+c.name;note="Bought "+c.name};Action("Sell city • "+c.value*80/100,c.name in owned){gold+=c.value*80/100;owned=owned-c.name}}}};Title("Countries");cities.map{it.country}.distinct().forEach{n->Card{Row(Modifier.padding(8.dp)){Column(Modifier.weight(1f)){Text(n,fontWeight=FontWeight.Bold);Text("Value 10,000")};Column{Action("Buy",n !in countries&&gold>=10000){gold-=10000;countries=countries+n};Action("Sell",n in countries){gold+=8000;countries=countries-n}}}}}}
 2->{Title("Shipyard");Text("Selected "+fleet[active].name+" • capacity "+capacity);Action("Upgrade capacity • 800",gold>=800){gold-=800;bonus+=25};boats.forEach{s->Action("Buy "+s.name+" • "+s.cost,gold>=s.cost){gold-=s.cost;fleet=fleet+s;active=fleet.lastIndex}};fleet.forEachIndexed{i,s->Action((if(i==active)"Active: " else "Select: ")+s.name,true){active=i;bonus=0}}}
 3->{Title("Market — "+city.name);Row{Text("Qty "+qty,Modifier.weight(1f));Action("−",true){qty=(qty-1).coerceAtLeast(1)};Action("+",true){qty=(qty+1).coerceAtMost(99)}};items.forEach{g->Card{Column(Modifier.padding(8.dp)){Text(g.name,fontWeight=FontWeight.Bold);Text("Price "+g.price+" • Cargo "+(cargo[g.name]?:0));Row{Button(enabled=gold>=g.price*qty&&total+qty<=capacity,onClick={gold-=g.price*qty;cargo=cargo+(g.name to ((cargo[g.name]?:0)+qty))},modifier=Modifier.weight(1f)){Text("Buy")};Button(enabled=(cargo[g.name]?:0)>=qty,onClick={gold+=g.price*qty;cargo=cargo+(g.name to ((cargo[g.name]?:0)-qty))},modifier=Modifier.weight(1f)){Text("Sell")}}}}}
 4->{Title("Army");Text("Army strength: "+power);listOf("Militia" to 250,"Infantry" to 600,"Cavalry" to 1200,"Siege" to 1800).forEach{u->Action("Recruit "+u.first+" • "+u.second,gold>=u.second){gold-=u.second;army=army+(u.first to ((army[u.first]?:0)+1))}};cities.filter{it.name!=city.name&&it.name !in owned}.forEach{c->Action("Attack "+c.name+" (defense "+c.defense+")",power>=c.defense){owned=owned+c.name;note="Conquered "+c.name}}}
 }
 Card{Column(Modifier.padding(10.dp)){Text("Annual income: "+income+" gold",fontWeight=FontWeight.Bold);Action("Advance year & collect",true){gold+=income;year++;note="Income collected"}}
 }}}
}
@Composable private fun Title(t:String){Text(t,fontWeight=FontWeight.Bold,modifier=Modifier.padding(vertical=4.dp))}
@Composable private fun Action(t:String,enabled:Boolean,click:()->Unit){Button(onClick=click,enabled=enabled,modifier=Modifier.fillMaxWidth().padding(vertical=2.dp)){Text(t)}}