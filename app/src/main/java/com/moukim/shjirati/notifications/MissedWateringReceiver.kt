package com.moukim.shjirati.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.moukim.shjirati.data.local.DatabaseProvider
import com.moukim.shjirati.domain.WateringCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MissedWateringReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val plantId = intent.getStringExtra(WateringAlarmReceiver.EXTRA_PLANT_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val plant = DatabaseProvider.get(context).dao().getPlant(plantId) ?: return@launch
                val today = LocalDate.now()
                val lastWatered = plant.lastWateredAtEpochMillis?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                if (lastWatered != today && WateringCalculator.isDueToday(plant, today.minusDays(1))) {
                    WateringAlarmReceiver.showReminder(
                        context,
                        plant.id,
                        plant.name,
                        plant.id.hashCode(),
                        "سقي فائت"
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
