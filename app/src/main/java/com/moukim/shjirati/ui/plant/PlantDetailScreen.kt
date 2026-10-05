package com.moukim.shjirati.ui.plant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moukim.shjirati.data.local.PlantCategory
import com.moukim.shjirati.data.local.PlantEntity
import com.moukim.shjirati.data.local.WateringLogEntity
import com.moukim.shjirati.domain.WateringCalculator
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PlantDetailScreen(
    plant: PlantEntity,
    history: Flow<List<WateringLogEntity>>,
    onBack: () -> Unit,
    onWater: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val logs by history.collectAsState(initial = emptyList())
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm").withZone(ZoneId.systemDefault())

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("حذف النبتة؟") },
            text = { Text("سيتم حذف النبتة من التطبيق. لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("حذف") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("إلغاء") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plant.name) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "رجوع") } },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "تعديل") }
                    TextButton(onClick = { confirmDelete = true }) { Text("حذف") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(plant.name, style = MaterialTheme.typography.headlineMedium)
                        Text(plant.category.arabicLabel(), style = MaterialTheme.typography.titleMedium)
                        plant.location?.let { Text("المكان: $it", style = MaterialTheme.typography.bodyLarge) }
                        plant.notes?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
                        HorizontalDivider()
                        Text(scheduleSummary(plant), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            item {
                Button(onClick = onWater, Modifier.fillMaxWidth().height(60.dp)) {
                    Icon(Icons.Default.WaterDrop, null)
                    Spacer(Modifier.width(10.dp))
                    Text("سقي الآن", style = MaterialTheme.typography.titleMedium)
                }
            }
            item { Text("سجل السقي", style = MaterialTheme.typography.headlineSmall) }
            if (logs.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Text("لا يوجد سجل سقي بعد.", Modifier.padding(20.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(logs, key = { it.id }) { log ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, null)
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(formatter.format(Instant.ofEpochMilli(log.wateredAtEpochMillis)), style = MaterialTheme.typography.titleMedium)
                                val details = buildList {
                                    log.amountMl?.let { add("$it مل") }
                                    log.durationMinutes?.let { add("$it دقيقة") }
                                }.joinToString(" • ")
                                if (details.isNotBlank()) Text(details, style = MaterialTheme.typography.bodyMedium)
                                log.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun scheduleSummary(plant: PlantEntity): String {
    if (plant.wateringDaysMask != 0) {
        val days = java.time.DayOfWeek.entries
            .filter { plant.wateringDaysMask and (1 shl (it.value - 1)) != 0 }
            .joinToString("، ") { it.arabicLabel() }
        return "السقي: $days — الساعة %02d:%02d".format(plant.wateringHour, plant.wateringMinute)
    }
    val interval = WateringCalculator.intervalFor(plant, java.time.LocalDate.now())
    val base = interval?.let { "كل $it يوم" } ?: "غير محدد"
    return "السقي: $base — الساعة %02d:%02d".format(plant.wateringHour, plant.wateringMinute)
}

private fun PlantCategory.arabicLabel() = when (this) {
    PlantCategory.TREE -> "شجرة"
    PlantCategory.SEEDLING -> "شتلة"
    PlantCategory.VEGETABLE -> "خضار"
    PlantCategory.OTHER -> "أخرى"
}

private fun java.time.DayOfWeek.arabicLabel() = when (this) {
    java.time.DayOfWeek.MONDAY -> "الإثنين"
    java.time.DayOfWeek.TUESDAY -> "الثلاثاء"
    java.time.DayOfWeek.WEDNESDAY -> "الأربعاء"
    java.time.DayOfWeek.THURSDAY -> "الخميس"
    java.time.DayOfWeek.FRIDAY -> "الجمعة"
    java.time.DayOfWeek.SATURDAY -> "السبت"
    java.time.DayOfWeek.SUNDAY -> "الأحد"
}