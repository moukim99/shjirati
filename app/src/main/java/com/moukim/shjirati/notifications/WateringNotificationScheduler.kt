package com.moukim.shjirati.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.moukim.shjirati.data.local.PlantEntity
import com.moukim.shjirati.domain.WateringCalculator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object WateringNotificationScheduler {
    private const val REQUEST_BASE = 20000

    fun schedule(context: Context, plant: PlantEntity) {
        val trigger = nextTrigger(plant)
        val pending = pendingIntent(context, plant)
        context.getSystemService(AlarmManager::class.java).cancel(pending)
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pending
        )
    }

    fun cancel(context: Context, plantId: String) {
        val intent = Intent(context, WateringAlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode(plantId),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(pending)
        pending.cancel()
    }

    private fun pendingIntent(context: Context, plant: PlantEntity): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode(plant.id),
            Intent(context, WateringAlarmReceiver::class.java)
                .putExtra(WateringAlarmReceiver.EXTRA_PLANT_ID, plant.id)
                .putExtra(WateringAlarmReceiver.EXTRA_PLANT_NAME, plant.name),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun nextTrigger(plant: PlantEntity): LocalDateTime {
        val now = LocalDateTime.now()
        var date = now.toLocalDate()
        repeat(370) {
            val candidate = LocalDateTime.of(date, java.time.LocalTime.of(plant.wateringHour, plant.wateringMinute))
            if (candidate.isAfter(now) && WateringCalculator.isDueToday(plant, date)) return candidate
            date = date.plusDays(1)
        }
        return LocalDateTime.of(now.toLocalDate().plusDays(1), java.time.LocalTime.of(plant.wateringHour, plant.wateringMinute))
    }

    private fun requestCode(id: String): Int = REQUEST_BASE + id.hashCode().and(0x7FFF)
}
