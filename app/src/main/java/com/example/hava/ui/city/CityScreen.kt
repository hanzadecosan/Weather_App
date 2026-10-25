package com.example.hava.ui.city

import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.luminance
import com.example.hava.ui.theme.SystemBarAppearance
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CityScreen(cities: List<String>, searchQuery: String, onSearchChange: (String) -> Unit, onCityClick: (String) -> Unit) {
    SystemBarAppearance(darkIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f)
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(24.dp))
            Text("HAVA / TÜRKİYE", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text("Gökyüzüne\nbir bakış.", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Text("Şehrinin havasını ve günün en güzel ışığını keşfet.",
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(value = searchQuery, onValueChange = onSearchChange,
                label = { Text("Şehir ara") }, singleLine = true,
                trailingIcon = { if (searchQuery.isNotEmpty()) TextButton(onClick = { onSearchChange("") }) { Text("Sil") } },
                shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Şehrini seç", style = MaterialTheme.typography.titleMedium)
                Text("${cities.size} şehir", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            if (cities.isEmpty()) {
                Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Şehir bulunamadı", style = MaterialTheme.typography.titleMedium)
                    Text("Farklı bir şehir adı yazmayı dene.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)) {
                    itemsIndexed(cities, key = { _, city -> city }) { index, city ->
                        CityCard(city, index, onClick = { onCityClick(city) })
                    }
                }
            }
        }
    }
}
