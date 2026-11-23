package com.oldworldtrader.game
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{Trader()}}}
private data class G(val name:String,val emoji:String,val price:Int)
private data class C(val name:String,val country:String,val emoji:String,val factor:Float,val defense:Int)
private data class S(val name:String,val emoji:String,val cost:Int,val capacity:Int)
private data class U(val name:String,val emoji:String,val cost:Int,val power:Int)
@Composable private fun Trader(){
 val goods=listOf(G("Spices","🌶️",90),G("Silk","🧵",150),G("Iron","⚒️",70),G("Wood","🪵",45),G("Wheat","🌾",30),G("Pearls","🫧",220))
 val cities=listOf(C("Alexandria","Egypt","🏛️",1.12f,20),C("Constantinople","Byzantine","🕌",1.28f,35),C("Muscat","Oman","🌴",.88f,15),C("Venice","Venetian Republic","⛵",1.35f,40),C("Delhi","Delhi Sultanate","🏰",.96f,25),C("Baghdad","Abbasid","🕋",1.05f,30),C("Damascus","Levant","🕌",1.1f,28),C("Samarkand","Central Asia","🐫",.92f,32))
 val ships=listOf(S("Small Boat","🛶",500,20),S("Merchant Ship","⛵",2500,60),S("Cargo Ship","🚢",8000,150),S("Large Vessel","⚓",20000,350))
 val units=listOf(U("Militia","🛡️",250,10),U("Infantry","⚔️",600,25),U("Cavalry","🐎",1200,45),U("Siege","🧱",1800,60))
 var tab by remember{mutableIntStateOf(0)};var city by remember{mutableStateOf(cities[0])};var gold by remember{mutableIntStateOf(1000)}
 var goodsBag by remember{mutableStateOf(mapOf<String,Int>())};var qty by remember{mutableIntStateOf(1)}
 var fleet by remember{mutableStateOf(listOf(ships[0]))};var active by remember{mutableIntStateOf(0)}
 var troops by remember{mutableStateOf(mapOf<String,Int>())};var captured by remember{mutableStateOf(setOf<String>())}
 var shop by remember{mutableStateOf(false)};var shopLv by remember{mutableIntStateOf(1)};var stock by remember{mutableStateOf(mapOf<String,Int>())}
 var year by remember{mutableIntStateOf(1)};var age by remember{mutableIntStateOf(18)};var msg by remember{mutableStateOf("Welcome! Trade goods, recruit army and capture cities.")}
 var shipBonuses by remember{mutableStateOf(mapOf<Int,Int>())}; var cityLevels by remember{mutableStateOf(mapOf<String,Int>())}; var education by remember{mutableStateOf(mapOf<String,Int>())}; var health by remember{mutableStateOf(mapOf<String,Int>())}; var spyNotes by remember{mutableStateOf(mapOf<String,String>())}; val counties=listOf("Nile Delta County","Cyprus County","Anatolia County","Persian Gulf County","Punjab County"); val ship=fleet.getOrElse(active){fleet.first()};val cargo=goodsBag.values.sum();val capacity=ship.capacity+(shipBonuses[active]?:0)
 val tabs=listOf("🏪 Shop","🏙️ Cities","🚢 Ships","📈 Market","⚔️ Army")
 MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF285743))){
 Column(Modifier.fillMaxSize().background(Color(0xFFF3EBDD))){
 Column(Modifier.fillMaxWidth().background(Color(0xFF1E342B)).padding(12.dp)){Text("⚓ OLD WORLD TRADER",color=Color(0xFFF4D49A),fontWeight=FontWeight.Black);Text("Gold "+gold+" • Cargo "+cargo+"/"+capacity+" • Fleet "+fleet.size+" • Age "+age,color=Color.White)}
 TabRow(selectedTabIndex=tab){tabs.forEachIndexed{i,t->Tab(selected=tab==i,onClick={tab=i},text={Text(t)})}}
 LazyColumn(contentPadding=PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
 item{Card{Column(Modifier.padding(12.dp)){Text("📍 "+city.emoji+" "+city.name+" ("+city.country+")",fontWeight=FontWeight.Bold);Text(msg)}}}
 when(tab){
 0->{item{Text("🏪 MERCHANT SHOP",fontWeight=FontWeight.Bold)};item{Card{Column(Modifier.padding(10.dp)){Text(if(shop)"Shop level "+shopLv else "Own a shop");Button(enabled=gold>=500&&!shop,onClick={gold-=500;shop=true;msg="Shop opened!"}){Text("Buy Shop • 500 gold")};goods.forEach{g->Button(enabled=shop&&(goodsBag[g.name]?:0)>0,onClick={goodsBag=goodsBag+(g.name to ((goodsBag[g.name]?:0)-1));stock=stock+(g.name to ((stock[g.name]?:0)+1))}){Text("Stock "+g.name+" ("+(stock[g.name]?:0)+")")}};Button(enabled=shop&&gold>=1500,onClick={gold-=1500;shopLv++;msg="Shop upgraded."}){Text("Upgrade Shop • 1500")}}}}}
 1->{item{Text("🏙️ CITIES & COUNTRIES",fontWeight=FontWeight.Bold)};cities.forEach{c->item{Card{Column(Modifier.padding(8.dp)){Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(c.emoji+" "+c.name,fontWeight=FontWeight.Bold);Text(c.country+" • market "+c.factor)};Button(onClick={city=c;msg="Travelled to "+c.name}){Text("Travel")}};Text("City level "+(cityLevels[c.name]?:0)+" • Education "+(education[c.name]?:0)+" • Health "+(health[c.name]?:0));Button(enabled=gold>=1000&&(cityLevels[c.name]?:0)<5,onClick={gold-=1000;cityLevels=cityLevels+(c.name to ((cityLevels[c.name]?:0)+1));msg="City upgraded"}){Text("🏗️ Upgrade city • 1000")};Row{Button(enabled=gold>=400,onClick={gold-=400;education=education+(c.name to ((education[c.name]?:0)+1));msg="Education improved"}){Text("📚 Education • 400")};Button(enabled=gold>=400,onClick={gold-=400;health=health+(c.name to ((health[c.name]?:0)+1));msg="Health improved"}){Text("🏥 Health • 400")}};Button(onClick={spyNotes=spyNotes+(c.name to ("Spy report: "+c.country+" • defense "+c.defense+" • market index "+c.factor+" • education "+(education[c.name]?:0)+" • health "+(health[c.name]?:0)));msg="Spy report received for "+c.name}){Text("🕵️ Spy report")};Text(spyNotes[c.name]?: "No intelligence report",color=Color.Gray)}}}}};item{Text("Conquered: "+captured.joinToString(),fontWeight=FontWeight.Bold)};item{Text("🗺️ County Development",fontWeight=FontWeight.Bold)};counties.forEach{county->item{Card{Column(Modifier.padding(8.dp)){Text(county,fontWeight=FontWeight.Bold);Text("Education "+(education[county]?:0)+" • Health "+(health[county]?:0));Button(enabled=gold>=700,onClick={gold-=700;education=education+(county to ((education[county]?:0)+1));msg="County school funded"}){Text("📚 Education • 700 gold")};Button(enabled=gold>=700,onClick={gold-=700;health=health+(county to ((health[county]?:0)+1));msg="County clinic funded"}){Text("🏥 Health • 700 gold")};Button(onClick={spyNotes=spyNotes+(county to "Report: local resources, morale, and defense summarized.");msg="County spy report completed"}){Text("🕵️ Intelligence report")};Text(spyNotes[county]?: "No spy report yet",color=Color.Gray)}}}}}
 2->{item{Text("🚢 SHIPYARD",fontWeight=FontWeight.Bold)};item{Card{Column(Modifier.padding(10.dp)){Text("Active vessel: "+ship.name+" • capacity "+capacity,fontWeight=FontWeight.Bold);Button(enabled=gold>=800,onClick={gold-=800;shipBonuses=shipBonuses+(active to ((shipBonuses[active]?:0)+25));msg="Ship upgraded: +25 cargo capacity"}){Text("🔧 Upgrade active ship • 800 gold (+25 cargo)")}}}};ships.forEach{s->item{Card{Row(Modifier.fillMaxWidth().padding(8.dp)){Column(Modifier.weight(1f)){Text(s.emoji+" "+s.name,fontWeight=FontWeight.Bold);Text("Cost "+s.cost+" • cargo "+s.capacity)};Button(enabled=gold>=s.cost,onClick={gold-=s.cost;fleet=fleet+s;active=fleet.lastIndex;msg="Bought "+s.name}){Text("Buy")}}}}};fleet.forEach{s->item{TextButton(onClick={active=fleet.indexOf(s);msg="Ship switched"}){Text("Set active: "+s.name)}}}}
 3->{item{Text("📈 MARKET • PRICE TREND",fontWeight=FontWeight.Bold)};item{Row{Text("Quantity "+qty,Modifier.weight(1f));TextButton(onClick={qty=(qty-1).coerceAtLeast(1)}){Text("−")};TextButton(onClick={qty=(qty+1).coerceAtMost(100)}){Text("+")}}};goods.forEach{g->item{val p=(g.price*city.factor).toInt();Card{Column(Modifier.padding(8.dp)){Text(g.emoji+" "+g.name,fontWeight=FontWeight.Bold);Text("Price: "+p+" gold • cargo "+(goodsBag[g.name]?:0));Text(if(city.factor>=1)"▲ Relatively high" else "▼ Relatively low",color=if(city.factor>=1)Color(0xFF26834A)else Color(0xFFAD5935));Row{Button(onClick={if(gold>=p*qty&&cargo+qty<=capacity){gold-=p*qty;goodsBag=goodsBag+(g.name to ((goodsBag[g.name]?:0)+qty));msg="Bought "+qty+" "+g.name}else msg="Gold/cargo limit!"}){Text("Buy")};Button(onClick={if((goodsBag[g.name]?:0)>=qty){gold+=p*qty;goodsBag=goodsBag+(g.name to ((goodsBag[g.name]?:0)-qty));msg="Sold goods for "+(p*qty)}else msg="Not enough stock"}){Text("Sell")}}}}}}
 4->{item{Text("⚔️ ARMY & CONQUEST",fontWeight=FontWeight.Bold)};item{Text("Army power: "+units.sumOf{it.power*(troops[it.name]?:0)})};units.forEach{u->item{Button(enabled=gold>=u.cost,onClick={gold-=u.cost;troops=troops+(u.name to ((troops[u.name]?:0)+1));msg="Recruited "+u.name}){Text(u.emoji+" Recruit "+u.name+" • "+u.cost+" gold • power "+u.power)}}};cities.filter{it.name!=city.name&&it.name !in captured}.forEach{target->item{val power=units.sumOf{it.power*(troops[it.name]?:0)};Card{Column(Modifier.padding(8.dp)){Text(target.emoji+" "+target.name+" • defense "+target.defense);Button(enabled=power>=target.defense,onClick={captured=captured+target.name;msg="Captured "+target.name+"! Passive income activated."}){Text("Conquer • power "+power)}}}}}}
 }
 item{Card{Column(Modifier.padding(10.dp)){Text("📅 Year "+year+" • Automatic income",fontWeight=FontWeight.Bold);Text("Territory income "+(captured.size*300)+" gold/year • shop income estimate "+(stock.values.sum()*shopLv*10));Button(onClick={val income=captured.size*300+stock.values.sum()*shopLv*10;gold+=income;year++;age++;msg="Yearly income collected: "+income+" gold"}){Text("Advance Year & collect income")}}}}
 }
 }
 }
}
