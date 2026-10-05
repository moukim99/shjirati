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
    private const val MISSED_REQUEST_BASE = 40000
    private const val SNOOZE_REQUEST_BASE = 60000

    fun schedule(context: Context, plant: PlantEntity) {
        val trigger = nextTrigger(plant)
        val pending = pendingIntent(context, plant)
        context.getSystemService(AlarmManager::class.java).cancel(pending)
        scheduleMissedCheck(context, plant)
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

    fun scheduleSnooze(context: Context, plant: PlantEntity, delayMinutes: Long = 60) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, SnoozeReceiver::class.java)
            .putExtra(WateringAlarmReceiver.EXTRA_PLANT_ID, plant.id)
            .putExtra(WateringAlarmReceiver.EXTRA_PLANT_NAME, plant.name)
        val pending = PendingIntent.getBroadcast(
            context,
            SNOOZE_REQUEST_BASE + plant.id.hashCode().and(0x7FFF),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + delayMinutes * 60_000L,
            pending
        )
    }

    private fun scheduleMissedCheck(context: Context, plant: PlantEntity) {
        val tomorrow = LocalDate.now().plusDays(1)
        val trigger = LocalDateTime.of(tomorrow, java.time.LocalTime.of(9, 0))
        val pending = PendingIntent.getBroadcast(
            context,
            MISSED_REQUEST_BASE + plant.id.hashCode().and(0x7FFF),
            Intent(context, MissedWateringReceiver::class.java)
                .putExtra(WateringAlarmReceiver.EXTRA_PLANT_ID, plant.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pending
        )
    }

    private fun requestCode(id: String): Int = REQUEST_BASE + id.hashCode().and(0x7FFF)
}
