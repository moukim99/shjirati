package com.moukim.shjirati.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moukim.shjirati.data.local.PlantEntity
import com.moukim.shjirati.domain.WateringCalculator

@Composable
fun HomeScreen(
    plants: List<PlantEntity>,
    onAddPlant: () -> Unit,
    onWater: (PlantEntity) -> Unit,
    onSelectPlant: (PlantEntity) -> Unit
) {
    val due = plants.filter { WateringCalculator.isDueToday(it) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPlant,
                text = { Text("إضافة نبتة") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("شجيراتي", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    when {
                        plants.isEmpty() -> "لنبدأ بإضافة أول نبتة."
                        due.isEmpty() -> "لا توجد نباتات تحتاج إلى السقي اليوم."
                        due.size == 1 -> "هناك نبتة واحدة تحتاج إلى السقي اليوم."
                        else -> "هناك ${due.size} نباتات تحتاج إلى السقي اليوم."
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (plants.isEmpty()) {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(42.dp))
                            Text("أضف نباتات حديقتك", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                "سجّل اسم النبتة وموعد السقي، وسيتولى التطبيق تذكيرك محلياً.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(onClick = onAddPlant, Modifier.fillMaxWidth().height(52.dp)) {
                                Text("إضافة أول نبتة")
                            }
                        }
                    }
                }
            } else {
                if (due.isNotEmpty()) {
                    item { Text("يحتاج إلى السقي اليوم", style = MaterialTheme.typography.headlineSmall) }
                    items(due, key = { it.id }) { plant ->
                        PlantCard(plant, true, onSelectPlant, onWater)
                    }
                }
                item { Text("نباتات أخرى", style = MaterialTheme.typography.headlineSmall) }
                items(plants.filter { it !in due }, key = { it.id }) { plant ->
                    PlantCard(plant, false, onSelectPlant, onWater)
                }
            }
        }
    }
}

@Composable
private fun PlantCard(
    plant: PlantEntity,
    dueToday: Boolean,
    onSelectPlant: (PlantEntity) -> Unit,
    onWater: (PlantEntity) -> Unit
) {
    Card(
        Modifier.fillMaxWidth().clickable { onSelectPlant(plant) },
        colors = CardDefaults.cardColors(
            containerColor = if (dueToday) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (dueToday) Icons.Default.WaterDrop else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(plant.name, style = MaterialTheme.typography.titleLarge)
                    plant.location?.let { Text("المكان: $it", style = MaterialTheme.typography.bodyLarge) }
                    Text(
                        if (dueToday) "مطلوب السقي اليوم" else "لا يحتاج إلى السقي الآن",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            if (dueToday) {
                Button(onClick = { onWater(plant) }, Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("تم السقي", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}