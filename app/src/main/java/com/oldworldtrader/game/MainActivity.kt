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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{TraderGame()}}}
private data class Good(val name:String,val emoji:String,val base:Int)
private data class City(val name:String,val country:String,val emoji:String,val factor:Float,val defense:Int,val wealth:Int)
private data class Ship(val name:String,val emoji:String,val price:Int,val capacity:Int)
private data class Troop(val name:String,val emoji:String,val cost:Int,val power:Int)
@Composable private fun TraderGame(){
 val goods=listOf(Good("Spices","🌶️",90),Good("Silk","🧵",150),Good("Iron","⚒️",70),Good("Wood","🪵",45),Good("Wheat","🌾",30),Good("Pearls","🫧",220))
 val cities=listOf(City("Alexandria","Egypt","🏛️",1.12f,20,6000),City("Constantinople","Byzantine Empire","🕌",1.28f,35,10000),City("Muscat","Oman","🌴",.88f,15,4500),City("Venice","Venetian Republic","⛵",1.35f,40,12000),City("Delhi","Delhi Sultanate","🏰",.96f,25,8000),City("Baghdad","Abbasid Caliphate","🕋",1.05f,30,9000),City("Damascus","Levant","🕌",1.1f,28,7500),City("Samarkand","Central Asia","🐫",.92f,32,7000))
 val ships=listOf(Ship("Small Boat","🛶",500,20),Ship("Merchant Ship","⛵",2500,60),Ship("Cargo Ship","🚢",8000,150),Ship("Large Vessel","⚓",20000,350))
 val units=listOf(Troop("Militia","🛡️",250,10),Troop("Infantry","⚔️",600,25),Troop("Cavalry","🐎",1200,45),Troop("Siege","🧱",1800,60))
 val countries=cities.map{it.country}.distinct()
 var tab by remember{mutableIntStateOf(0)};var gold by remember{mutableIntStateOf(1000)};var year by remember{mutableIntStateOf(1)};var age by remember{mutableIntStateOf(18)}
 var city by remember{mutableStateOf(cities[0])};var cargo by remember{mutableStateOf(mapOf<String,Int>())};var qty by remember{mutableIntStateOf(1)}
 var fleet by remember{mutableStateOf(listOf(ships[0]))};var active by remember{mutableIntStateOf(0)};var upgrades by remember{mutableStateOf(mapOf<Int,Int>())}
 var shop by remember{mutableStateOf(false)};var shopLv by remember{mutableIntStateOf(0)};var stock by remember{mutableStateOf(mapOf<String,Int>())}
 var army by remember{mutableStateOf(mapOf<String,Int>())};var ownedCities by remember{mutableStateOf(setOf<String>())};var ownedCountries by remember{mutableStateOf(setOf<String>())}
 var cityLv by remember{mutableStateOf(mapOf<String,Int>())};var edu by remember{mutableStateOf(mapOf<String,Int>())};var health by remember{mutableStateOf(mapOf<String,Int>())};var spy by remember{mutableStateOf(mapOf<String,String>())};var message by remember{mutableStateOf("Trade, develop cities, buy territories and build an empire.")}
 val ship=fleet.getOrElse(active){fleet.first()};val capacity=ship.capacity+(upgrades[active]?:0);val cargoTotal=cargo.values.sum();val power=units.sumOf{it.power*(army[it.name]?:0)}
 val tabs=listOf("🏪 Shop","🏙️ Cities","🚢 Ships","📈 Market","⚔️ Army")
 MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF285743))){
 Column(Modifier.fillMaxSize().background(Color(0xFFF3EBDD))){
 Column(Modifier.fillMaxWidth().background(Color(0xFF1E342B)).padding(10.dp)){Text("⚓ OLD WORLD TRADER",color=Color(0xFFF2D49A),fontWeight=FontWeight.Bold);Text("💰 "+gold+" • 📦 "+cargoTotal+"/"+capacity+" • 🚢 "+fleet.size+" • Year "+year+" • Age "+age,color=Color.White)}
 TabRow(selectedTabIndex=tab){tabs.forEachIndexed{i,t->Tab(selected=tab==i,onClick={tab=i},text={Text(t)})}}
 Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(10.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
 Card{Column(Modifier.padding(10.dp)){Text("📍 "+city.emoji+" "+city.name+" — "+city.country,fontWeight=FontWeight.Bold);Text(message)}}
 when(tab){
 0->{Section("🏪 Merchant shop");Card{Column(Modifier.padding(10.dp)){Text(if(shop)"Shop Level "+shopLv else "Open your shop");Btn("Buy shop • 500 gold",gold>=500&&!shop){gold-=500;shop=true;shopLv=1;message="Shop purchased."};if(shop){goods.forEach{g->Btn("Stock "+g.emoji+" "+g.name+" ("+(stock[g.name]?:0)+")",(cargo[g.name]?:0)>0){cargo=cargo+(g.name to ((cargo[g.name]?:0)-1));stock=stock+(g.name to ((stock[g.name]?:0)+1))}};Btn("Upgrade shop • 1,500",gold>=1500){gold-=1500;shopLv++;message="Shop upgraded."}}}}}
 1->{Section("🏙️ Cities & Country assets");cities.forEach{c->Card{Column(Modifier.padding(9.dp)){Text(c.emoji+" "+c.name,fontWeight=FontWeight.Bold);Text("Country: "+c.country+" • Wealth: "+(if(c.name in ownedCities)c.wealth*115/100 else c.wealth)+" gold • Market index "+c.factor);Row{Btn("Travel",true){city=c;message="Arrived at "+c.name};Btn("Buy city • "+c.wealth,c.name !in ownedCities&&gold>=c.wealth){gold-=c.wealth;ownedCities=ownedCities+c.name;message="City purchased."};Btn("Sell city • "+(c.wealth*80/100),c.name in ownedCities){gold+=c.wealth*80/100;ownedCities=ownedCities-c.name;message="City sold."}};if(c.name in ownedCities){Text("Owned city • annual income "+(c.wealth/20+(cityLv[c.name]?:0)*150));Btn("Upgrade city • 1,000",gold>=1000&&(cityLv[c.name]?:0)<5){gold-=1000;cityLv=cityLv+(c.name to ((cityLv[c.name]?:0)+1))};Row{Btn("📚 Education • 400",gold>=400){gold-=400;edu=edu+(c.name to ((edu[c.name]?:0)+1))};Btn("🏥 Health • 400",gold>=400){gold-=400;health=health+(c.name to ((health[c.name]?:0)+1))}};Text("Education "+(edu[c.name]?:0)+" • Health "+(health[c.name]?:0));Btn("🕵️ Spy report",true){spy=spy+(c.name to ("Report: country "+c.country+", defense "+c.defense+", wealth "+c.wealth+", market "+c.factor+", education "+(edu[c.name]?:0)+", health "+(health[c.name]?:0)));message="Spy report prepared."};Text(spy[c.name]?: "No spy intel yet")}}};Section("🌍 Countries buy/sell");countries.forEach{n->Card{Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(n,fontWeight=FontWeight.Bold);Text("Wealth: "+(if(n in ownedCountries)15000 else 10000)+" gold")};Column{Btn("Buy • 10,000",n !in ownedCountries&&gold>=10000){gold-=10000;ownedCountries=ownedCountries+n;message="Country acquired: "+n};Btn("Sell • 8,000",n in ownedCountries){gold+=8000;ownedCountries=ownedCountries-n;message="Country sold: "+n}}}}};Text("Empire wealth: "+(ownedCities.sumOf{n->cities.find{it.name==n}?.wealth?:0}+ownedCountries.size*10000)+" gold",fontWeight=FontWeight.Bold)}
 2->{Section("🚢 Shipyard");Card{Column(Modifier.padding(10.dp)){Text("Active ship: "+ship.name+" • Capacity "+capacity);Btn("Upgrade active • 800 gold (+25 capacity)",gold>=800){gold-=800;upgrades=upgrades+(active to ((upgrades[active]?:0)+25));message="Ship capacity increased."}}};ships.forEach{s->Card{Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(s.emoji+" "+s.name,fontWeight=FontWeight.Bold);Text("Price "+s.price+" • Capacity "+s.capacity)};Btn("Buy",gold>=s.price){gold-=s.price;fleet=fleet+s;active=fleet.lastIndex;message="Ship bought."}}}};fleet.forEachIndexed{i,s->Btn((if(i==active)"ACTIVE: "else "Select: ")+s.name,true){active=i;message="Active vessel changed."}}}
 3->{Section("📈 Market");Row{Text("Qty "+qty,Modifier.weight(1f));Btn("−",true){qty=(qty-1).coerceAtLeast(1)};Btn("+",true){qty=(qty+1).coerceAtMost(100)}};goods.forEach{g->val p=(g.base*city.factor).toInt();Card{Column(Modifier.padding(10.dp)){Text(g.emoji+" "+g.name,fontWeight=FontWeight.Bold);Text("Price: "+p+" gold • cargo "+(cargo[g.name]?:0));Text(if(city.factor>=1f)"▲ Higher price index" else "▼ Lower price index",color=if(city.factor>=1f)Color(0xFF24804A)else Color(0xFFB65B36));Row{Btn("Buy",gold>=p*qty&&cargoTotal+qty<=capacity){gold-=p*qty;cargo=cargo+(g.name to ((cargo[g.name]?:0)+qty));message="Bought goods."};Btn("Sell",(cargo[g.name]?:0)>=qty){gold+=p*qty;cargo=cargo+(g.name to ((cargo[g.name]?:0)-qty));message="Sale total "+p*qty+" gold"}}}}}
 4->{Section("⚔️ Army recruitment & attacks");Text("Army power: "+power);units.forEach{u->Btn(u.emoji+" Recruit "+u.name+" • "+u.cost+" gold • power "+u.power,gold>=u.cost){gold-=u.cost;army=army+(u.name to ((army[u.name]?:0)+1))}};cities.filter{it.name!=city.name&&it.name !in ownedCities}.forEach{c->Card{Column(Modifier.padding(8.dp)){Text("Attack "+c.name+" • defense "+c.defense);Btn("Conquer • Army power "+power,power>=c.defense){ownedCities=ownedCities+c.name;message="Conquered "+c.name+"! Passive income enabled."}}}}}
 }
 val annual=ownedCities.sumOf{n->(cities.find{it.name==n}?.wealth?:0)/20+(cityLv[n]?:0)*150}+ownedCountries.size*500+stock.values.sum()*shopLv*10
 Card{Column(Modifier.padding(10.dp)){Text("📅 Year "+year+" • Annual income "+annual+" gold",fontWeight=FontWeight.Bold);Btn("Advance year • collect income",true){gold+=annual;year++;age++;message="Collected yearly income: "+annual+" gold."}}}
 }
 }
 }
}
@Composable private fun Section(title:String){Text(title,fontWeight=FontWeight.Bold,color=Color(0xFF263D30),modifier=Modifier.padding(vertical=3.dp))}
@Composable private fun Btn(label:String,enabled:Boolean,onClick:()->Unit){Button(enabled=enabled,onClick=onClick,modifier=Modifier.padding(2.dp)){Text(label)}}
