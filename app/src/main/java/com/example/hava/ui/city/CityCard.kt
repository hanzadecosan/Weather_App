package com.example.hava.ui.city

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Stable name-based styling: filtering never changes a city's identity.
// These are decorative palettes, deliberately not a claim about live weather.
private val lightPalettes = listOf(
    listOf(Color(0xFFFFE7B1), Color(0xFFF5B87F)),
    listOf(Color(0xFFE0EAF7), Color(0xFFABC2DF)),
    listOf(Color(0xFFF0E4F4), Color(0xFFCAB6DF)),
    listOf(Color(0xFFE5EDDB), Color(0xFFB9D1BB))
)
private val darkPalettes = listOf(
    listOf(Color(0xFF4D3B44), Color(0xFF785647)),
    listOf(Color(0xFF24344C), Color(0xFF3F5A7D)),
    listOf(Color(0xFF36324D), Color(0xFF5C4D77)),
    listOf(Color(0xFF253E41), Color(0xFF3F6264))
)

@Composable
fun CityCard(cityName: String, onClick: () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    val paletteIndex = Math.floorMod(cityName.hashCode(), lightPalettes.size)
    val palette = (if (dark) darkPalettes else lightPalettes)[paletteIndex]
    val ink = if (dark) Color(0xFFF5F0EC) else Color(0xFF34313A)
    Card(onClick = onClick, shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().heightIn(min = 168.dp)) {
        Box(Modifier.fillMaxWidth().heightIn(min = 168.dp).background(Brush.linearGradient(palette))) {
            Canvas(Modifier.matchParentSize()) {
                val center = Offset(size.width*.95f, size.height*.28f)
                drawCircle(Color.White.copy(alpha = .18f), radius=size.width*.36f, center=center)
                drawCircle(Color.White.copy(alpha = .27f), radius=size.width*.51f, center=center, style=Stroke(1.dp.toPx()))
                drawCircle(Color.White.copy(alpha = .15f), radius=size.width*.70f, center=center, style=Stroke(1.dp.toPx()))
            }
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("TÜRKİYE", color = ink.copy(alpha=.62f), fontSize=8.sp, letterSpacing=1.5.sp)
                    Box(Modifier.size(28.dp).background(Color.White.copy(alpha=.25f),CircleShape), contentAlignment=Alignment.Center) {
                        Text("↗", color=ink,fontSize=17.sp)
                    }
                }
                Spacer(Modifier.height(27.dp))
                Text(cityName, color=ink, fontSize=20.sp, lineHeight=24.sp, fontWeight=FontWeight.Medium,
                    maxLines=2, overflow=TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                Text("Şehri keşfet", color=ink.copy(alpha=.65f), fontSize=10.sp)
            }
        }
    }
}
