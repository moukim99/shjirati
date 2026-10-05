package com.moukim.shjirati.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.moukim.shjirati.R
import com.moukim.shjirati.data.local.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WateringAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_PLANT_ID = "plant_id"
        const val EXTRA_PLANT_NAME = "plant_name"
        const val EXTRA_SNOOZE_ACTION = "snooze_action"
        private const val CHANNEL_ID = "watering_reminders"

        fun showReminder(context: Context, name: String, id: Int, title: String = "وقت سقي النباتات") {
            createChannel(context)
            if (android.os.Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return

            val snoozeIntent = Intent(context, SnoozeReceiver::class.java)
                .putExtra(EXTRA_PLANT_ID, id.toString())
                .putExtra(EXTRA_SNOOZE_ACTION, true)
            val snoozePending = PendingIntent.getBroadcast(
                context,
                70000 + id.and(0x7FFF),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText("حان وقت سقي $name")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(0, "تأجيل ساعة", snoozePending)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(id, notification)
        }

        private fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تذكيرات السقي",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تذكيرات سقي النباتات"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val plantId = intent.getStringExtra(EXTRA_PLANT_ID) ?: return

        if (intent.getBooleanExtra(EXTRA_SNOOZE_ACTION, false)) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val plant = DatabaseProvider.get(context).dao().getPlant(plantId)
                    if (plant != null) WateringNotificationScheduler.scheduleSnooze(context, plant)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val plant = DatabaseProvider.get(context).dao().getPlant(plantId)
                if (plant != null) {
                    showReminder(context, plant.name, plant.id.hashCode())
                    WateringNotificationScheduler.schedule(context, plant)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
