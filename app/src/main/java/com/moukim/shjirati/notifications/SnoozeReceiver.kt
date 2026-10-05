package com.moukim.shjirati.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.moukim.shjirati.data.local.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val plantId = intent.getStringExtra(WateringAlarmReceiver.EXTRA_PLANT_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val plant = DatabaseProvider.get(context).dao().getPlant(plantId)
                if (plant != null) {
                    WateringAlarmReceiver.showReminder(context, plant.name, plant.id.hashCode(), "تذكير مؤجل")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
