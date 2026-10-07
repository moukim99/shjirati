package com.moukim.shjirati.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moukim.shjirati.data.local.PlantCategory
import com.moukim.shjirati.data.local.PlantEntity
import com.moukim.shjirati.domain.PlantAgeCalculator
import com.moukim.shjirati.domain.WateringCalculator
import com.moukim.shjirati.ui.theme.*
import com.moukim.shjirati.util.ImageUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HomeScreen(
    plants: List<PlantEntity>,
    onAddPlant: () -> Unit,
    onWater: (PlantEntity) -> Unit,
    onSelectPlant: (PlantEntity) -> Unit,
    onGardenLocation: () -> Unit,
    onWeather: () -> Unit,
    onAssistant: () -> Unit
) {
    val due = remember(plants) { plants.filter { WateringCalculator.isDueToday(it) } }
    val notDue = remember(plants, due) { plants.filter { it !in due } }

    Scaffold(
        containerColor = AppBackground,
        floatingActionButtonPosition = FabPosition.Start,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPlant,
                shape = RoundedCornerShape(16.dp),
                containerColor = AppFabBg,
                contentColor = AppFabText,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                ),
                text = {
                    Text(
                        text = "إضافة نبتة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppFabText
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AppFabText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Section
            item(span = { GridItemSpan(2) }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0x20000000),
                            ambientColor = Color(0x0F000000)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    color = AppCard.copy(alpha = 0.9f),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f)),
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AppPrimaryBrown.copy(alpha = 0.12f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = AppPrimaryBrown,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onGardenLocation,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("موقع الحديقة")
                        }
                        OutlinedButton(
                            onClick = onWeather,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("الطقس")
                        }
        OutlinedButton(
                            onClick = onAssistant,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("المساعد")
                        }
                        Text(
                            text = "شجيراتي",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTextMain,
                            lineHeight = 38.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = when {
                                plants.isEmpty() -> "لنبدأ بإضافة أول نبتة."
                                due.isEmpty() -> "لا توجد نباتات تحتاج إلى السقي اليوم."
                                due.size == 1 -> "هناك نبتة واحدة تحتاج إلى السقي اليوم."
                                else -> "هناك ${due.size} نباتات تحتاج إلى السقي اليوم."
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppTextMuted,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                DailySummaryCard(plants = plants, dueCount = due.size)
            }

            if (plants.isEmpty()) {
                // Empty State Card
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppCard.copy(alpha = 0.7f)
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = AppWaterDrop,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "أضف نباتات حديقتك",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTextMain
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "سجّل اسم النبتة وموعد السقي، وسيتولى التطبيق تذكيرك محلياً.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = AppTextMuted.copy(alpha = 0.95f),
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Button(
                                onClick = onAddPlant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppPrimaryBrown,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 1.dp
                                )
                            ) {
                                Text(
                                    text = "إضافة أول نبتة",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                if (due.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = AppFabBg,
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
                                    color = AppFabText.copy(alpha = 0.12f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = null,
                                            tint = AppFabText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "يحتاج إلى السقي اليوم",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppFabText
                                )
                            }
                        }
                    }
                    items(due, key = { "due_${it.id}" }) { plant ->
                        PlantCard(
                            plant = plant,
                            dueToday = true,
                            onSelectPlant = onSelectPlant,
                            onWater = onWater
                        )
                    }
                }
                items(notDue, key = { "notdue_${it.id}" }) { plant ->
                    PlantCard(
                        plant = plant,
                        dueToday = false,
                        onSelectPlant = onSelectPlant,
                        onWater = onWater
                    )
                }
            }
        }
    }
}

@Composable
@Composable
private fun DailySummaryCard(plants: List<PlantEntity>, dueCount: Int) {
    val today = LocalDate.now()
    val wateredToday = plants.count { plant ->
        plant.lastWateredAtEpochMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() == today
        } == true
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("ملخص اليوم", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = AppTextMain)
            Text(
                text = when {
                    plants.isEmpty() -> "أضف أول نبتة للبدء."
                    dueCount > 0 && wateredToday > 0 -> "بقي سقي " + dueCount + " نبتة، وتم سقي " + wateredToday + " اليوم."
                    dueCount > 0 -> "لديك " + dueCount + " نبتة تحتاج إلى السقي اليوم."
                    wateredToday > 0 -> "تم سقي " + wateredToday + " نبتة اليوم."
                    else -> "لا توجد مهام سقي مسجلة لليوم حتى الآن."
                },
                fontSize = 14.sp, color = AppTextMuted, lineHeight = 21.sp
            )
        }
    }
}
private fun PlantCard(
    plant: PlantEntity,
    dueToday: Boolean,
    onSelectPlant: (PlantEntity) -> Unit,
    onWater: (PlantEntity) -> Unit
) {
    val coverPath = remember(plant.imageUri) {
        plant.imageUrisList.firstOrNull()
    }
    val imageBitmap = remember(coverPath) {
        coverPath?.let { ImageUtils.loadThumbnailBitmap(it, maxDimension = 360) }
    }

    val calculatedAge = remember(plant.plantedAtEpochMillis, plant.category) {
        PlantAgeCalculator.calculateAge(plant.plantedAtEpochMillis, plant.category)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectPlant(plant) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (dueToday) AppFabBg else Color.White
        ),
        border = BorderStroke(1.dp, PlantBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (imageBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                ) {
                    Image(
                        bitmap = imageBitmap.asImageBitmap(),
                        contentDescription = plant.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (plant.icon != null) {
                        Surface(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.TopStart),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = plant.icon,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (plant.imageUrisList.size > 1) {
                        Surface(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.TopEnd),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${plant.imageUrisList.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .background(
                            color = if (dueToday) AppPrimaryBrown.copy(alpha = 0.1f) else PlantLavender.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (plant.icon != null) {
                        Text(
                            text = plant.icon,
                            fontSize = 44.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = if (dueToday) AppPrimaryBrown else AppWaterDrop,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = plant.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (dueToday) Icons.Default.WaterDrop else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (dueToday) AppPrimaryBrown else AppWaterDrop,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = plant.category.arabicLabel(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTextMuted
                )

                if (calculatedAge != null) {
                    Text(
                        text = "العمر: $calculatedAge",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppPrimaryBrown,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                plant.location?.let { loc ->
                    if (loc.isNotBlank()) {
                        Text(
                            text = "المكان: $loc",
                            fontSize = 11.sp,
                            color = AppTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (dueToday) {
                    Button(
                        onClick = { onWater(plant) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppPrimaryBrown,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "سقي",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun PlantCategory.arabicLabel() = when (this) {
    PlantCategory.TREE -> "🌳 شجرة"
    PlantCategory.VEGETABLE -> "🥕 خضروات"
    PlantCategory.HERB -> "🌿 أعشاب"
}

@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyPreview() {
    ShjiratiTheme {
        HomeScreen(
            plants = emptyList(),
            onAddPlant = {},
            onWater = {},
            onSelectPlant = {},
            onGardenLocation = {}
        )
    }
}
