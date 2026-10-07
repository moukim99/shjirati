package com.moukim.shjirati.ui.weather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudySnowing
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moukim.shjirati.data.weather.WeatherDaily
import com.moukim.shjirati.data.weather.WeatherLocation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeatherScreen(
    location: WeatherLocation,
    days: List<WeatherDaily>,
    isRefreshing: Boolean,
    isOffline: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onBack: () -> Unit = {}
) {
    val today = days.firstOrNull()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الطقس", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Text("‹", fontSize = 34.sp) } },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث الطقس")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { StatusBanner(isOffline, errorMessage, today?.downloadedAtEpochMillis) }
            item { CurrentWeatherCard(today) }
            item { Text("التوقعات القادمة", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            items(days.take(14), key = { it.dateEpochDay }) { day -> WeatherDayCard(day) }
            item {
                Text(
                    text = "الموقع: %.4f, %.4f".format(Locale.US, location.latitude, location.longitude),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StatusBanner(isOffline: Boolean, errorMessage: String?, downloadedAt: Long?) {
    val text = when {
        isOffline && errorMessage != null -> "لا يوجد اتصال. نعرض آخر بيانات محفوظة."
        isOffline -> "وضع عدم الاتصال: البيانات من الذاكرة المحلية."
        downloadedAt != null -> "تم تحديث بيانات الطقس وحفظها محليًا."
        else -> "لم يتم تحميل بيانات الطقس بعد."
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isOffline) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(if (isOffline) Icons.Default.CloudQueue else Icons.Default.Cloud, null)
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CurrentWeatherCard(day: WeatherDaily?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("اليوم", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DeviceThermostat, null, modifier = Modifier.size(48.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = day?.temperatureMaxC?.let { "%.0f°".format(Locale.US, it) } ?: "—",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "العظمى\n" + (day?.temperatureMinC?.let { "%.0f°".format(Locale.US, it) } ?: "—") + " الصغرى",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Metric(Icons.Default.WaterDrop, "الأمطار", day?.precipitationMm?.let { "%.1f مم".format(Locale.US, it) } ?: "—")
                Metric(Icons.Default.Opacity, "احتمال المطر", day?.precipitationProbabilityPercent?.let { "$it%" } ?: "—")
                Metric(Icons.Default.WindPower, "الرياح", day?.windSpeedMaxKmh?.let { "%.0f كم/س".format(Locale.US, it) } ?: "—")
            }
            if (day?.frostRisk == true || day?.heatRisk == true) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = when {
                            day.frostRisk && day.heatRisk -> "تنبيه: خطر صقيع وحرارة مرتفعة"
                            day.frostRisk -> "تنبيه: احتمال الصقيع"
                            else -> "تنبيه: حرارة مرتفعة"
                        },
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun Metric(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WeatherDayCard(day: WeatherDaily) {
    val date = LocalDate.ofEpochDay(day.dateEpochDay)
    val title = if (date == LocalDate.now()) "اليوم" else date.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale("ar")))
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(
                    text = listOfNotNull(
                        day.precipitationMm?.let { "مطر %.1f مم".format(Locale.US, it) },
                        day.humidityMeanPercent?.let { "رطوبة %.0f%%".format(Locale.US, it) }
                    ).joinToString(" • "),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (day.frostRisk) Icon(Icons.Default.CloudySnowing, "صقيع")
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${day.temperatureMaxC?.let { "%.0f°".format(Locale.US, it) } ?: "—"} / ${day.temperatureMinC?.let { "%.0f°".format(Locale.US, it) } ?: "—"}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
