package com.moukim.shjirati.ui.plant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moukim.shjirati.data.local.PlantCategory
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantFormScreen(
    onSave: (String, PlantCategory, String?, String?, Int?, Int, Int, Int, Boolean, Int?, Int?, Int?, Int?) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PlantCategory.TREE) }
    var useWeekdays by remember { mutableStateOf(false) }
    var intervalText by remember { mutableStateOf("3") }
    var selectedDays by remember { mutableStateOf(setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY)) }
    var showTimePicker by remember { mutableStateOf(false) }
    var wateringHour by remember { mutableIntStateOf(18) }
    var wateringMinute by remember { mutableIntStateOf(0) }
    var seasonalEnabled by remember { mutableStateOf(false) }
    var springInterval by remember { mutableStateOf("") }
    var summerInterval by remember { mutableStateOf("") }
    var autumnInterval by remember { mutableStateOf("") }
    var winterInterval by remember { mutableStateOf("") }

    val timeState = rememberTimePickerState(
        initialHour = wateringHour,
        initialMinute = wateringMinute,
        is24Hour = true
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    wateringHour = timeState.hour
                    wateringMinute = timeState.minute
                    showTimePicker = false
                }) { Text("تم") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("إلغاء") }
            },
            title = { Text("وقت التذكير") },
            text = { TimePicker(state = timeState) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إضافة نبتة") },
                navigationIcon = { TextButton(onClick = onBack) { Text("رجوع") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                name, { name = it }, Modifier.fillMaxWidth(),
                label = { Text("اسم النبتة") },
                singleLine = true
            )

            Text("نوع النبتة", style = MaterialTheme.typography.titleMedium)
            PlantCategory.entries.forEach { item ->
                FilterChip(
                    selected = category == item,
                    onClick = { category = item },
                    label = { Text(item.arabicLabel()) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                location, { location = it }, Modifier.fillMaxWidth(),
                label = { Text("مكانها في الحديقة (اختياري)") },
                singleLine = true
            )
            OutlinedTextField(
                notes, { notes = it }, Modifier.fillMaxWidth(),
                label = { Text("ملاحظات (اختياري)") },
                minLines = 3
            )

            HorizontalDivider()

            Text("جدول السقي", style = MaterialTheme.typography.titleLarge)
            Text(
                "اختر الطريقة الأسهل لتذكيرك بالسقي.",
                style = MaterialTheme.typography.bodyLarge
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!useWeekdays) {
                    Button(onClick = { useWeekdays = false }, Modifier.weight(1f)) {
                        Text("كل عدة أيام")
                    }
                    OutlinedButton(onClick = { useWeekdays = true }, Modifier.weight(1f)) {
                        Text("أيام محددة")
                    }
                } else {
                    OutlinedButton(onClick = { useWeekdays = false }, Modifier.weight(1f)) {
                        Text("كل عدة أيام")
                    }
                    Button(onClick = { useWeekdays = true }, Modifier.weight(1f)) {
                        Text("أيام محددة")
                    }
                }
            }

            if (!useWeekdays) {
                OutlinedTextField(
                    value = intervalText,
                    onValueChange = { value ->
                        if (value.all(Char::isDigit) && value.length <= 2) intervalText = value
                    },
                    Modifier.fillMaxWidth(),
                    label = { Text("السقي كل كم يوم؟") },
                    supportingText = { Text("مثال: 3 يعني السقي كل ثلاثة أيام") },
                    singleLine = true
                )
            } else {
                Text("اختر أيام السقي", style = MaterialTheme.typography.titleMedium)
                val days = DayOfWeek.entries
                days.chunked(2).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { day ->
                            FilterChip(
                                selected = day in selectedDays,
                                onClick = {
                                    selectedDays = if (day in selectedDays) {
                                        selectedDays - day
                                    } else {
                                        selectedDays + day
                                    }
                                },
                                label = { Text(day.arabicLabel()) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                if (selectedDays.isEmpty()) {
                    Text(
                        "اختر يوماً واحداً على الأقل.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            OutlinedButton(
                onClick = { showTimePicker = true },
                Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("وقت التذكير: %02d:%02d".format(wateringHour, wateringMinute))
            }

            Button(
                onClick = {
                    val interval = intervalText.toIntOrNull()?.coerceIn(1, 99)
                    val mask = if (useWeekdays) {
                        selectedDays.fold(0) { mask, day ->
                            mask or (1 shl (day.value - 1))
                        }
                    } else 0
                    onSave(
                        name.trim(),
                        category,
                        location.trim().ifBlank { null },
                        notes.trim().ifBlank { null },
                        if (useWeekdays) null else interval,
                        mask,
                        wateringHour,
                        wateringMinute
                    )
                },
                enabled = name.isNotBlank() &&
                    (useWeekdays && selectedDays.isNotEmpty() || !useWeekdays && intervalText.toIntOrNull()?.let { it > 0 } == true),
                Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("حفظ النبتة", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun PlantCategory.arabicLabel() = when (this) {
    PlantCategory.TREE -> "شجرة"
    PlantCategory.SEEDLING -> "شتلة"
    PlantCategory.VEGETABLE -> "خضار"
    PlantCategory.OTHER -> "أخرى"
}

private fun DayOfWeek.arabicLabel() = when (this) {
    DayOfWeek.MONDAY -> "الإثنين"
    DayOfWeek.TUESDAY -> "الثلاثاء"
    DayOfWeek.WEDNESDAY -> "الأربعاء"
    DayOfWeek.THURSDAY -> "الخميس"
    DayOfWeek.FRIDAY -> "الجمعة"
    DayOfWeek.SATURDAY -> "السبت"
    DayOfWeek.SUNDAY -> "الأحد"
}
