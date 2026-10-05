package com.moukim.shjirati.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moukim.shjirati.data.local.PlantEntity
import com.moukim.shjirati.domain.WateringCalculator

@Composable
fun HomeScreen(plants: List<PlantEntity>, onAddPlant: () -> Unit, onWater: (PlantEntity) -> Unit, onSelectPlant: (PlantEntity) -> Unit) {
    val due = plants.filter { WateringCalculator.isDueToday(it) }
    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = onAddPlant) {
            Icon(Icons.Default.Add, contentDescription = "إضافة نبتة")
        }
    }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("شجيراتي", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (due.isEmpty()) "لا توجد نباتات تحتاج إلى السقي اليوم"
                    else "اليوم تحتاج إلى سقي " + due.size + " نبتة",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (due.isEmpty() && plants.isNotEmpty()) {\n                item { Text("كل نباتاتك", style = MaterialTheme.typography.headlineSmall) }\n                items(plants, key = { it.id }) { plant ->\n                    PlantCard(plant, onSelectPlant, onWater)\n                }\n            } else {\n                items(due, key = { it.id }) { plant ->
                Card(Modifier.fillMaxWidth().clickable { onSelectPlant(plant) }) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, null, Modifier.size(34.dp))
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(plant.name, style = MaterialTheme.typography.titleLarge)
                            plant.location?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
                        }
                        Button(onClick = { onWater(plant) }) { Text("تم السقي") }
                    }
                }
            }
        }
    }
}
