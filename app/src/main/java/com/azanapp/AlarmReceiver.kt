package com.azanapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.azanapp.PRAYER_ALARM" -> {
                val name = intent.getStringExtra("prayer_name") ?: return
                val time = intent.getStringExtra("prayer_time") ?: return
                NotificationHelper(context).apply {
                    createNotificationChannel()
                    showPrayerNotification(name, time)
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<PrayerWorker>().build())
            }
        }
    }
}
