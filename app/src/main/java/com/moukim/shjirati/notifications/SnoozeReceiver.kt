package com.moukim.shjirati.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.moukim.shjirati.data.local.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val plantId = intent.getStringExtra(WateringAlarmReceiver.EXTRA_PLANT_ID) ?: return
        val notificationId = intent.getIntExtra(MarkWateredReceiver.EXTRA_NOTIFICATION_ID, plantId.hashCode())

        NotificationManagerCompat.from(context).cancel(notificationId)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val plant = DatabaseProvider.get(context).dao().getPlant(plantId)
                if (plant != null) {
                    if (intent.getBooleanExtra(WateringAlarmReceiver.EXTRA_SNOOZE_ACTION, false)) {
                        WateringNotificationScheduler.scheduleSnooze(context, plant)
                    } else {
                        WateringAlarmReceiver.showReminder(
                            context = context,
                            plant = plant,
                            id = plant.id.hashCode(),
                            title = "تذكير مؤجل ⏰"
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
