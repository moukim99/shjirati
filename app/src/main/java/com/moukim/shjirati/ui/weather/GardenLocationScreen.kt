package com.moukim.shjirati.ui.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moukim.shjirati.data.weather.WeatherLocation
import java.util.Locale

@Composable
fun GardenLocationScreen(
    initialLocation: WeatherLocation?,
    onSave: (WeatherLocation) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit
) {
    var latitude by rememberSaveable { mutableStateOf(initialLocation?.latitude?.toInput() ?: "") }
    var longitude by rememberSaveable { mutableStateOf(initialLocation?.longitude?.toInput() ?: "") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun save() {
        val lat = latitude.toDoubleOrNull()
        val lon = longitude.toDoubleOrNull()
        error = when {
            lat == null || lon == null -> "أدخل خط العرض وخط الطول بشكل صحيح."
            lat !in -90.0..90.0 -> "خط العرض يجب أن يكون بين -90 و 90."
            lon !in -180.0..180.0 -> "خط الطول يجب أن يكون بين -180 و 180."
            else -> null
        }
        if (error == null) onSave(WeatherLocation(lat!!, lon!!))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("موقع الحديقة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("رجوع") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp)
                    )
                    Text("حدد موقع حديقتك", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "يُحفظ الموقع داخل الهاتف ويُستخدم لجلب طقس الحديقة وحساب التنبيهات الخاصة بنباتاتك.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }

            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it; error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("خط العرض") },
                placeholder = { Text("مثال: 36.2639") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it; error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("خط الطول") },
                placeholder = { Text("مثال: 6.6200") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = ::save,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("حفظ موقع الحديقة")
            }

            if (initialLocation != null) {
                OutlinedButton(
                    onClick = {
                        onClear()
                        latitude = ""
                        longitude = ""
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("حذف الموقع المحفوظ")
                }
            }

            Text(
                "ملاحظة: لا يتم طلب صلاحية الموقع تلقائيًا. يمكنك إدخال الإحداثيات يدويًا.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Double.toInput(): String =
    String.format(Locale.US, "%.6f", this).trimEnd('0').trimEnd('.')
