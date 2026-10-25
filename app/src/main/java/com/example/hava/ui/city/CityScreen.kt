package com.example.hava.ui.city

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hava.ui.theme.SystemBarAppearance
import com.example.hava.ui.weather.AnimatedWeatherBackground
import com.example.hava.ui.weather.WeatherKind
import com.example.hava.ui.weather.WeatherMotion

@Composable
fun CityScreen(cities: List<String>, searchQuery: String, onSearchChange: (String) -> Unit, onCityClick: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    SystemBarAppearance(darkIcons = colors.background.luminance() > .5f)
    Surface(color = colors.background) {
        LazyVerticalGrid(columns = GridCells.Adaptive(156.dp),
            modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding(),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item(key = "intro", span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("hava.", fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.5).sp)
                        Text("GÖKYÜZÜNE BİR BAKIŞ", fontSize = 9.sp, letterSpacing = 1.8.sp, color = colors.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(20.dp))
                    CityDiscoveryHero()
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(value = searchQuery, onValueChange = onSearchChange,
                        label = { Text("Şehir ara") }, placeholder = { Text("Bugün nereye bakalım?") },
                        singleLine = true, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = colors.outline.copy(alpha=.35f)),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) TextButton(onClick = { onSearchChange("") }) { Text("Sil") }
                            else Text("⌕", modifier = Modifier.padding(end = 16.dp), fontSize = 26.sp)
                        })
                    if (searchQuery.isBlank()) {
                        Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("İstanbul", "Ankara", "İzmir").forEach { city ->
                                SuggestionChip(onClick = { onCityClick(city) }, label = { Text("$city ↗", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(18.dp))
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(if (searchQuery.isBlank()) "Şehirleri keşfet" else "Arama sonuçları",
                            fontSize = 22.sp, fontWeight = FontWeight.Medium, letterSpacing = (-.5).sp)
                        Text("${cities.size} şehir", color = colors.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
            if (cities.isEmpty()) {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) { EmptyCitySearch() }
            } else {
                items(cities, key = { it }) { city -> CityCard(cityName = city, onClick = { onCityClick(city) }) }
            }
            item(key = "footer", span = { GridItemSpan(maxLineSpan) }) {
                Text("81 şehir. Her gün başka bir gökyüzü.", modifier = Modifier.padding(top = 12.dp),
                    color = colors.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CityDiscoveryHero() {
    val shape = RoundedCornerShape(32.dp)
    Box(Modifier.fillMaxWidth().height(218.dp).clip(shape)
        .background(Brush.linearGradient(listOf(Color(0xFFFFE4A8), Color(0xFFFFB66D))))) {
        AnimatedWeatherBackground(WeatherKind.SUNNY, true, Modifier.matchParentSize())
        WeatherMotion(WeatherKind.SUNNY, modifier = Modifier.align(Alignment.CenterEnd).offset(x = 24.dp, y = (-8).dp).size(156.dp))
        Column(Modifier.padding(22.dp).width(220.dp)) {
            Text("BUGÜNÜN ROTASI", color = Color(0xFF77552D), fontSize = 9.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(14.dp))
            Text("Bir şehir.\nBin gökyüzü.", color = Color(0xFF372A20), fontSize = 31.sp,
                lineHeight = 34.sp, letterSpacing = (-1).sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(14.dp))
            Text("Havayı hisset.\nGünün ışığını yakala.", color = Color(0xFF6F5945), fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun EmptyCitySearch() {
    Column(Modifier.fillMaxWidth().padding(vertical = 34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        WeatherMotion(WeatherKind.CLOUDY, modifier = Modifier.size(90.dp))
        Text("Şehir bulunamadı", style = MaterialTheme.typography.titleMedium)
        Text("Farklı bir şehir adı yazmayı dene.", color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
    }
}
