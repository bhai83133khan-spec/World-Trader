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
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{TraderApp()}}}
private data class Good(val name:String,val price:Int)
private data class City(val name:String,val country:String,val value:Int,val defense:Int)
private data class Ship(val name:String,val cost:Int,val capacity:Int)
private data class Troop(val name:String,val cost:Int,val power:Int)
@Composable private fun TraderApp(){
 val goods=listOf(Good("Spices",90),Good("Silk",150),Good("Iron",70),Good("Wood",45),Good("Wheat",30),Good("Pearls",220))
 val cities=listOf(City("Alexandria","Egypt",6000,20),City("Constantinople","Byzantine Empire",10000,35),City("Muscat","Oman",4500,15),City("Venice","Venetian Republic",12000,40),City("Delhi","Delhi Sultanate",8000,25),City("Baghdad","Abbasid Caliphate",9000,30),City("Damascus","Levant",7500,28),City("Samarkand","Central Asia",7000,32))
 val ships=listOf(Ship("Small Boat",500,20),Ship("Merchant Ship",2500,60),Ship("Cargo Ship",8000,150),Ship("Large Vessel",20000,350))
 val troops=listOf(Troop("Militia",250,10),Troop("Infantry",600,25),Troop("Cavalry",1200,45),Troop("Siege",1800,60))
 var tab by remember{mutableIntStateOf(0)};var gold by remember{mutableIntStateOf(1000)};var year by remember{mutableIntStateOf(1)}
 var city by remember{mutableStateOf(cities.first())};var cargo by remember{mutableStateOf(mapOf<String,Int>())};var qty by remember{mutableIntStateOf(1)}
 var fleet by remember{mutableStateOf(listOf(ships.first()))};var selectedShip by remember{mutableIntStateOf(0)};var bonus by remember{mutableIntStateOf(0)}
 var ownedCities by remember{mutableStateOf(setOf<String>())};var ownedCountries by remember{mutableStateOf(setOf<String>())};var army by remember{mutableStateOf(mapOf<String,Int>())};var shopLevel by remember{mutableIntStateOf(0)};var stock by remember{mutableStateOf(mapOf<String,Int>())};var msg by remember{mutableStateOf("Trade goods to grow your empire.")}
 val capacity=fleet[selectedShip.coerceIn(0,fleet.lastIndex)].capacity+bonus;val cargoTotal=cargo.values.sum();val power=troops.sumOf{it.power*(army[it.name]?:0)}
 val income=ownedCities.sumOf{n->(cities.find{it.name==n}?.value?:0)/20}+ownedCountries.size*500+stock.values.sum()*shopLevel*10
 MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF285743))){Column(Modifier.fillMaxSize().background(Color(0xFFF3EBDD))){
 Column(Modifier.fillMaxWidth().background(Color(0xFF1E342B)).padding(12.dp)){Text("⚓ OLD WORLD TRADER",color=Color(0xFFF2D49A),fontWeight=FontWeight.Bold);Text("Gold $gold • Cargo $cargoTotal/$capacity • Ships ${fleet.size} • Year $year",color=Color.White)}
 ScrollableTabRow(selectedTabIndex=tab){listOf("Shop","Cities","Ships","Market","Army").forEachIndexed{i,s->Tab(selected=tab==i,onClick={tab=i},text={Text(s)})}}
 Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(10.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Card{Column(Modifier.padding(10.dp)){Text("Current city: ${city.name} — ${city.country}",fontWeight=FontWeight.Bold);Text(msg)}}
 when(tab){
 0->ShopScreen(goods,gold,shopLevel,stock,cargo,onOpen={gold-=500;shopLevel=1;msg="Shop opened."},onStock={g->cargo=cargo+(g.name to ((cargo[g.name]?:0)-1));stock=stock+(g.name to ((stock[g.name]?:0)+1))},onUpgrade={gold-=1500;shopLevel++})
 1->CitiesScreen(cities,city,gold,ownedCities,ownedCountries,onTravel={city=it;msg="Travelled to ${it.name}"},onBuy={c->gold-=c.value;ownedCities=ownedCities+c.name;msg="Purchased ${c.name}"},onSell={c->gold+=c.value*80/100;ownedCities=ownedCities-c.name},onBuyCountry={n->gold-=10000;ownedCountries=ownedCountries+n},onSellCountry={n->gold+=8000;ownedCountries=ownedCountries-n})
 2->ShipsScreen(ships,fleet,selectedShip,gold,bonus,onBuy={s->gold-=s.cost;fleet=fleet+s;selectedShip=fleet.lastIndex},onSelect={selectedShip=it;bonus=0},onUpgrade={gold-=800;bonus+=25})
 3->MarketScreen(goods,city,gold,cargo,cargoTotal,capacity,qty,onQty={qty=(qty+it).coerceIn(1,100)},onBuy={g,q->gold-=g.price*q;cargo=cargo+(g.name to ((cargo[g.name]?:0)+q));msg="Bought ${g.name}"},onSell={g,q->gold+=g.price*q;cargo=cargo+(g.name to ((cargo[g.name]?:0)-q));msg="Sold ${g.name}"})
 else->ArmyScreen(troops,cities,city,gold,power,ownedCities,onRecruit={t->gold-=t.cost;army=army+(t.name to ((army[t.name]?:0)+1))},onConquer={c->ownedCities=ownedCities+c.name;msg="Conquered ${c.name}"})
 Card{Column(Modifier.padding(10.dp)){Text("Annual income: $income gold",fontWeight=FontWeight.Bold);Button(onClick={gold+=income;year++;msg="Collected yearly income"}){Text("Advance year & collect")}}}
 }
 }
}
@Composable private fun ShopScreen(goods:List<Good>,gold:Int,level:Int,stock:Map<String,Int>,cargo:Map<String,Int>,onOpen:()->Unit,onStock:(Good)->Unit,onUpgrade:()->Unit){
 Text("🏪 Business & stock",fontWeight=FontWeight.Bold)
 if(level==0)Action("Open shop • 500 gold",gold>=500,onOpen)
 if(level>0){Text("Shop level $level");goods.forEach{g->Action("Stock ${g.name} • owned ${stock[g.name]?:0}",(cargo[g.name]?:0)>0){onStock(g)}};Action("Upgrade shop • 1,500",gold>=1500,onUpgrade)}
}
@Composable private fun CitiesScreen(cities:List<City>,current:City,gold:Int,owned:Set<String>,countries:Set<String>,onTravel:(City)->Unit,onBuy:(City)->Unit,onSell:(City)->Unit,onBuyCountry:(String)->Unit,onSellCountry:(String)->Unit){
 Text("🏙️ Cities: buy, sell, travel",fontWeight=FontWeight.Bold)
 cities.forEach{c->Card{Column(Modifier.padding(8.dp)){Text("${c.name} • ${c.country}",fontWeight=FontWeight.Bold);Text("Wealth ${c.value} • Defense ${c.defense}");Action("Travel",true){onTravel(c)};Action("Buy city • ${c.value}",c.name !in owned&&gold>=c.value){onBuy(c)};Action("Sell city • ${c.value*80/100}",c.name in owned){onSell(c)}}}}
 Text("🌍 Country ownership",fontWeight=FontWeight.Bold)
 cities.map{it.country}.distinct().forEach{n->Card{Row(Modifier.padding(8.dp)){Column(Modifier.weight(1f)){Text(n,fontWeight=FontWeight.Bold);Text("Value 10,000 gold")};Column{Action("Buy",n !in countries&&gold>=10000){onBuyCountry(n)};Action("Sell • 8,000",n in countries){onSellCountry(n)}}}}}
}
@Composable private fun ShipsScreen(ships:List<Ship>,fleet:List<Ship>,selected:Int,gold:Int,bonus:Int,onBuy:(Ship)->Unit,onSelect:(Int)->Unit,onUpgrade:()->Unit){
 Text("🚢 Shipyard • Fleet",fontWeight=FontWeight.Bold);Text("Selected ${fleet[selected.coerceIn(0,fleet.lastIndex)].name} • capacity ${fleet[selected.coerceIn(0,fleet.lastIndex)].capacity+bonus}");Action("Upgrade capacity • 800 (+25)",gold>=800,onUpgrade)
 ships.forEach{s->Card{Row(Modifier.padding(8.dp)){Column(Modifier.weight(1f)){Text(s.name,fontWeight=FontWeight.Bold);Text("Cost ${s.cost} • Capacity ${s.capacity}")};Action("Buy",gold>=s.cost){onBuy(s)}}}}
 fleet.forEachIndexed{i,s->Action((if(i==selected)"ACTIVE: " else "Select: ")+s.name,true){onSelect(i)}}
}
@Composable private fun MarketScreen(goods:List<Good>,city:City,gold:Int,cargo:Map<String,Int>,total:Int,capacity:Int,qty:Int,onQty:(Int)->Unit,onBuy:(Good,Int)->Unit,onSell:(Good,Int)->Unit){
 Text("📈 Market — ${city.name}",fontWeight=FontWeight.Bold);Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Text("Quantity $qty",Modifier.weight(1f));Action("−",true){onQty(-1)};Action("+",true){onQty(1)}}
 goods.forEach{g->Card{Column(Modifier.padding(8.dp)){Text(g.name,fontWeight=FontWeight.Bold);Text("Price ${g.price} gold • Cargo ${cargo[g.name]?:0}");Row{Button(enabled=gold>=g.price*qty&&total+qty<=capacity,onClick={onBuy(g,qty)},modifier=Modifier.weight(1f)){Text("Buy")};Button(enabled=(cargo[g.name]?:0)>=qty,onClick={onSell(g,qty)},modifier=Modifier.weight(1f)){Text("Sell")}}}}}
}
@Composable private fun ArmyScreen(troops:List<Troop>,cities:List<City>,current:City,gold:Int,power:Int,owned:Set<String>,onRecruit:(Troop)->Unit,onConquer:(City)->Unit){
 Text("⚔️ Army & Conquest • power $power",fontWeight=FontWeight.Bold);troops.forEach{t->Action("Recruit ${t.name} • ${t.cost} • power ${t.power}",gold>=t.cost){onRecruit(t)}}
 cities.filter{it.name!=current.name&&it.name !in owned}.forEach{c->Card{Column(Modifier.padding(8.dp)){Text("Attack ${c.name} • defense ${c.defense}");Action("Conquer",power>=c.defense){onConquer(c)}}}}
}
@Composable private fun Action(label:String,enabled:Boolean,onClick:()->Unit){Button(onClick=onClick,enabled=enabled,modifier=Modifier.fillMaxWidth().padding(vertical=2.dp)){Text(label)}}