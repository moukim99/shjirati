package com.moukim.shjirati.ui.plant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moukim.shjirati.data.local.PlantCategory

@Composable
fun PlantFormScreen(onSave: (String, PlantCategory, String?, String?) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PlantCategory.TREE) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("إضافة نبتة") }, navigationIcon = {
            TextButton(onClick = onBack) { Text("رجوع") }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("اسم النبتة") })
            Text("نوع النبتة", style = MaterialTheme.typography.titleMedium)
            PlantCategory.entries.forEach { item ->
                FilterChip(category == item, { category = item }, label = { Text(item.arabicLabel()) })
            }
            OutlinedTextField(location, { location = it }, Modifier.fillMaxWidth(), label = { Text("مكانها في الحديقة (اختياري)") })
            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("ملاحظات (اختياري)") }, minLines = 3)
            Button(
                onClick = { onSave(name.trim(), category, location.trim().ifBlank { null }, notes.trim().ifBlank { null }) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("حفظ النبتة") }
        }
    }
}

private fun PlantCategory.arabicLabel() = when (this) {
    PlantCategory.TREE -> "شجرة"
    PlantCategory.SEEDLING -> "شتلة"
    PlantCategory.VEGETABLE -> "خضار"
    PlantCategory.OTHER -> "أخرى"
}
