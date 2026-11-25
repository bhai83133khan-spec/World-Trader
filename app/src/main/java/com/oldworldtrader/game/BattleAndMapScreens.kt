package com.oldworldtrader.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.min

private val Parchment = Color(0xFFF2E5C7)
private val Ink = Color(0xFF2D261D)
private val Gold = Color(0xFFD5AA58)
private val Forest = Color(0xFF24493C)

/** Reusable battle archive with a focused detail view for player and AI encounters. */
@Composable
fun BattleReportScreen(
    reports: List<BattleReport>,
    onSelectCity: (String) -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("CHRONICLES OF WAR", style = MaterialTheme.typography.headlineSmall,
            color = Ink, fontWeight = FontWeight.Black)
        Text("Recent sieges, raids and campaigns", color = Forest)
        if (reports.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Text("No battles recorded. Enemy kingdoms will act as years pass.", Modifier.padding(18.dp))
            }
        }
        reports.asReversed().forEachIndexed { index, report ->
            BattleReportCard(report = report, battleNumber = reports.size - index, onSelectCity = onSelectCity)
        }
    }
}

@Composable
private fun BattleReportCard(report: BattleReport, battleNumber: Int, onSelectCity: (String) -> Unit) {
    val resultColor = if (report.victory) Forest else Color(0xFF8F3028)
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.background(Brush.verticalGradient(listOf(Parchment, Color(0xFFE7D2A9))))
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${if (report.isSiege) "SIEGE" else "BATTLE"} • ${battleNumber.toString().padStart(2, '0')}", color = Ink, fontWeight = FontWeight.Bold)
                Text(if (report.victory) "VICTORY" else "DEFEAT", color = resultColor, fontWeight = FontWeight.Black)
            }
            Text("${report.attacker}  ⚔  ${report.defender}", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = Ink)
            BattleStrengthBar("ATTACK", report.attackerStrength, report.attackerCasualties, report.attackerSurvivors, resultColor)
            BattleStrengthBar("DEFENSE", report.defenderStrength, report.defenderCasualties,
                report.defenderSurvivors, Color(0xFF496783),
                lossesLabel = if (report.isSiege) "Defense damage" else "Casualties",
                survivorsLabel = if (report.isSiege) "Remaining defense" else "Survivors")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Spoils: ${report.loot} gold", fontWeight = FontWeight.Bold, color = Color(0xFF76500D))
                report.capturedCity?.let { captured ->
                    Text("Captured: ${captured.name}", Modifier.clickable { onSelectCity(captured.id) },
                        color = Forest, fontWeight = FontWeight.Bold)
                } ?: Text("Territory: held")
            }
            Text(report.summary, color = Ink, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BattleStrengthBar(
    label: String, strength: Int, losses: Int, survivors: Int, color: Color,
    lossesLabel: String = "Casualties", survivorsLabel: String = "Survivors"
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Ink, fontWeight = FontWeight.Bold)
            Text("Strength $strength", color = Ink)
        }
        Box(Modifier.fillMaxWidth().height(9.dp).background(Color(0xFFB6A98E), RoundedCornerShape(8.dp))) {
            Box(Modifier.fillMaxWidth(fraction = if (strength <= 0) 0f else
                (survivors.toFloat() / strength.toFloat()).coerceIn(0f, 1f))
                .height(9.dp).background(color, RoundedCornerShape(8.dp)))
        }
        Text("$lossesLabel $losses  •  $survivorsLabel $survivors", color = Ink,
            style = MaterialTheme.typography.labelMedium)
    }
}

/** Stylized original vector parchment map; markers show ownership and open city detail on tap. */
@Composable
fun WorldMapScreen(cities: List<CityData>, onCitySelected: (CityData) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("THE KNOWN WORLD", style = MaterialTheme.typography.headlineSmall,
            color = Ink, fontWeight = FontWeight.Black)
        Text("Trade routes, contested frontiers and sovereign ports", color = Forest)
        Card(Modifier.fillMaxWidth().height(290.dp), shape = RoundedCornerShape(20.dp)) {
            Box(Modifier.fillMaxSize().background(Color(0xFFC9D8CC))) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawRect(Brush.verticalGradient(listOf(Color(0xFFB9D2D1), Color(0xFFDAE0CF))))
                    val land = Path().apply {
                        moveTo(w * .05f, h * .22f); lineTo(w * .22f, h * .12f)
                        lineTo(w * .34f, h * .2f); lineTo(w * .44f, h * .15f)
                        lineTo(w * .55f, h * .25f); lineTo(w * .73f, h * .21f)
                        lineTo(w * .92f, h * .32f); lineTo(w * .84f, h * .53f)
                        lineTo(w * .69f, h * .49f); lineTo(w * .6f, h * .7f)
                        lineTo(w * .44f, h * .64f); lineTo(w * .35f, h * .84f)
                        lineTo(w * .21f, h * .67f); lineTo(w * .08f, h * .58f); close()
                    }
                    drawPath(land, Color(0xFFB9A77C))
                    drawPath(land, Color(0xFF816B43), style = Stroke(width = 3.dp.toPx()))
                    // Decorative compass rose and trade route stitches.
                    val route = Path().apply {
                        moveTo(w * .19f, h * .43f); cubicTo(w * .38f, h * .23f, w * .55f, h * .72f, w * .77f, h * .4f)
                    }
                    drawPath(route, Color(0xFF8B4333), style = Stroke(width = 2.dp.toPx()))
                    val compass = Offset(w * .88f, h * .76f)
                    drawLine(Gold, Offset(compass.x, compass.y - 20), Offset(compass.x, compass.y + 20), 2.dp.toPx())
                    drawLine(Gold, Offset(compass.x - 16, compass.y), Offset(compass.x + 16, compass.y), 2.dp.toPx())
                }
                cities.forEachIndexed { index, city ->
                    val x = .16f + (index % 4) * .19f
                    val y = .37f + (index % 3) * .14f
                    val marker = if (city.controlledBy == "player" || city.owned) Gold else Color(0xFF8C382F)
                    Column(Modifier.align(Alignment.TopStart)
                        .padding(start = (x * 100).dp, top = (y * 100).dp)
                        .clickable { onCitySelected(city) }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✦", color = marker, fontWeight = FontWeight.Black)
                        Text(city.name, color = Ink, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        cities.forEach { city ->
            Card(Modifier.fillMaxWidth().clickable { onCitySelected(city) }) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("${city.name} • ${city.country}", color = Ink, fontWeight = FontWeight.Bold)
                        Text("${city.population} residents • ${if (city.controlledBy == null) "Independent" else "Controlled by ${city.controlledBy}"}")
                    }
                    Text(if (city.owned) "CROWN" else "PORT", color = Forest, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
