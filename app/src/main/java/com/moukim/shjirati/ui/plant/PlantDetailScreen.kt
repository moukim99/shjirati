package com.moukim.shjirati.ui.plant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moukim.shjirati.data.catalog.PlantArticle
import com.moukim.shjirati.data.local.PlantCategory
import com.moukim.shjirati.data.local.PlantEntity
import com.moukim.shjirati.data.local.WateringLogEntity
import com.moukim.shjirati.domain.PlantAgeCalculator
import com.moukim.shjirati.domain.WateringCalculator
import com.moukim.shjirati.util.ImageUtils
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plant: PlantEntity,
    article: PlantArticle? = null,
    history: Flow<List<WateringLogEntity>>,
    onBack: () -> Unit,
    onWater: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val logs by history.collectAsState(initial = emptyList())
    
    val imagePaths = remember(plant.imageUri) { plant.imageUrisList }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    val currentPath = imagePaths.getOrNull(selectedImageIndex) ?: imagePaths.firstOrNull()
    val mainBitmap = remember(currentPath) {
        currentPath?.let { ImageUtils.loadThumbnailBitmap(it, maxDimension = 1080) }
    }
    
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd - HH:mm").withZone(ZoneId.systemDefault())
    val dueToday = WateringCalculator.isDueToday(plant)

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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                shadowElevation = 6.dp,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        if (plant.icon != null) {
                            Text(text = plant.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = plant.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = { confirmDelete = true },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "حذف",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
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
                        if (mainBitmap != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Image(
                                    bitmap = mainBitmap.asImageBitmap(),
                                    contentDescription = "صورة النبتة",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(230.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                if (imagePaths.size > 1) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(imagePaths.size) { idx ->
                                            val path = imagePaths[idx]
                                            val thumbBitmap = remember(path) {
                                                ImageUtils.loadThumbnailBitmap(path, maxDimension = 180)
                                            }
                                            val isSelected = idx == selectedImageIndex
                                            if (thumbBitmap != null) {
                                                Image(
                                                    bitmap = thumbBitmap.asImageBitmap(),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(56.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .border(
                                                            width = if (isSelected) 2.dp else 0.dp,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .clickable { selectedImageIndex = idx },
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (plant.icon != null) {
                                Text(plant.icon, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(plant.name, style = MaterialTheme.typography.headlineMedium)
                        }

                        Text(plant.category.arabicLabel(), style = MaterialTheme.typography.titleMedium)

                        plant.location?.let { Text("المكان: $it", style = MaterialTheme.typography.bodyLarge) }

                        plant.plantedAtEpochMillis?.let { epoch ->
                            val date = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDate()
                            Text("تاريخ الغرس: %04d/%02d/%02d".format(date.year, date.monthValue, date.dayOfMonth), style = MaterialTheme.typography.bodyLarge)

                            val ageText = PlantAgeCalculator.calculateAge(epoch, plant.category)
                            if (ageText != null) {
                                Text("العمر الحالي: $ageText", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        plant.expectedDateEpochMillis?.let { epoch ->
                            val date = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDate()
                            val label = if (plant.category == PlantCategory.TREE) "تاريخ الحصاد المتوقع" else "تاريخ الإنبات المتوقع"
                            Text("$label: %04d/%02d/%02d".format(date.year, date.monthValue, date.dayOfMonth), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }

                        plant.notes?.let { Text("ملاحظات: $it", style = MaterialTheme.typography.bodyLarge) }

                        HorizontalDivider()

                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = if (dueToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(if (dueToday) "يحتاج إلى السقي اليوم" else "لا يحتاج إلى السقي الآن", style = MaterialTheme.typography.titleMedium)
                                Text(scheduleSummary(plant), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
            article?.let { knowledge ->
                item {
                    KnowledgeSection(title = "عن النبتة") {
                        KnowledgeRow("الوصف", knowledge.description)
                        KnowledgeRow("الموطن الأصلي", knowledge.origin)
                        KnowledgeRow("النوع", knowledge.plantType)
                        KnowledgeRow("طريقة الإكثار", knowledge.propagation)
                    }
                }
                item {
                    KnowledgeSection(title = "التربة والظروف المناسبة") {
                        KnowledgeRow("التربة", knowledge.soil)
                        KnowledgeRow("درجة الحموضة", knowledge.soilPh)
                        KnowledgeRow("الإضاءة", knowledge.sunlight)
                        KnowledgeRow("الري", knowledge.watering)
                        KnowledgeRow("الحرارة", knowledge.temperature)
                        KnowledgeRow("تحمل الصقيع", knowledge.frostTolerance)
                        KnowledgeRow("تحمل الحرارة", knowledge.heatTolerance)
                    }
                }
                item {
                    KnowledgeSection(title = "الزراعة والنمو") {
                        KnowledgeRow("الموسم", knowledge.plantingSeason)
                        KnowledgeRow("طريقة الزراعة", knowledge.plantingMethod)
                        KnowledgeRow("عمق البذور", knowledge.seedDepth)
                        KnowledgeRow("المسافة", knowledge.spacing)
                        KnowledgeRow("الإنبات", knowledge.germination)
                        KnowledgeRow("النمو والحصاد", knowledge.growthHarvest)
                        KnowledgeRow("التسميد", knowledge.fertilization)
                    }
                }
                item {
                    KnowledgeSection(title = "المشكلات والملاحظات") {
                        KnowledgeRow("الآفات والأمراض", knowledge.pestsDiseases)
                        KnowledgeRow("ملاحظات", knowledge.notes)
                    }
                }
            }
            item {
                Button(onClick = onWater, Modifier.fillMaxWidth().height(64.dp)) {
                    Icon(Icons.Default.WaterDrop, null)
                    Spacer(Modifier.width(10.dp))
                    Text("سقي الآن", style = MaterialTheme.typography.titleMedium)
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "سجل السقي",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
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
    PlantCategory.TREE -> "🌳 شجرة"
    PlantCategory.VEGETABLE -> "🥕 خضروات"
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


@Composable
private fun KnowledgeSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun KnowledgeRow(label: String, value: String) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
