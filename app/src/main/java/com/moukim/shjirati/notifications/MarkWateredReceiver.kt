package com.moukim.shjirati.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.moukim.shjirati.data.local.DatabaseProvider
import com.moukim.shjirati.data.local.WateringLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class MarkWateredReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val plantId = intent.getStringExtra(WateringAlarmReceiver.EXTRA_PLANT_ID) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, plantId.hashCode())

        NotificationManagerCompat.from(context).cancel(notificationId)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = DatabaseProvider.get(context).dao()
                val plant = dao.getPlant(plantId)
                if (plant != null) {
                    val now = System.currentTimeMillis()
                    dao.insertWateringLog(
                        WateringLogEntity(
                            id = UUID.randomUUID().toString(),
                            plantId = plant.id,
                            wateredAtEpochMillis = now,
                            amountMl = plant.wateringAmountMl,
                            durationMinutes = plant.wateringDurationMinutes
                        )
                    )
                    val updated = plant.copy(
                        lastWateredAtEpochMillis = now,
                        updatedAtEpochMillis = now
                    )
                    dao.upsertPlant(updated)
                    WateringNotificationScheduler.schedule(context, updated)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
