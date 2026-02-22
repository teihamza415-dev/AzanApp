package com.azanapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {
    companion object { const val CHANNEL_ID = "azan_channel" }

    fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "أذان الصلاة", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "إشعارات مواقيت الصلاة"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun showPrayerNotification(prayerName: String, prayerTime: String) {
        val intent = Intent(context, PrayerTimesActivity::class.java)
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("🕌 حان وقت $prayerName")
            .setContentText("وقت صلاة $prayerName: $prayerTime")
            .setStyle(NotificationCompat.BigTextStyle().bigText("حان الآن وقت صلاة $prayerName\nالوقت: $prayerTime"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .build()
        context.getSystemService(NotificationManager::class.java).notify(prayerName.hashCode(), notification)
    }
}
