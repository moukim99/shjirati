package com.moukim.shjirati.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WateringAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_PLANT_ID = "plant_id"
        const val EXTRA_PLANT_NAME = "plant_name"
        private const val CHANNEL_ID = "watering_reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val plantId = intent.getStringExtra(EXTRA_PLANT_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = DatabaseProvider.get(context).dao()
                val plant = dao.getPlant(plantId)
                if (plant != null) {
                    createChannel(context)
                    showNotification(context, plant.name, plant.id.hashCode())
                    WateringNotificationScheduler.schedule(context, plant)
                }
            } finally {
                pendingResult.finish()
            }
        }
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

    private fun showNotification(context: Context, name: String, id: Int) {
        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("وقت سقي النباتات")
            .setContentText("حان وقت سقي $name")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
