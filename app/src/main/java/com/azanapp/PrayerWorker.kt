package com.azanapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.*

class PrayerWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val loc = LocationHelper(context).getCurrentLocation() ?: return Result.failure()
            val resp = PrayerApiService.create().getPrayerTimes(loc.first, loc.second)
            if (resp.isSuccessful) {
                val timings = resp.body()?.data?.timings ?: return Result.failure()
                scheduleAlarms(timings)
                saveTimings(timings)
                Result.success()
            } else Result.retry()
        } catch (e: Exception) { Result.retry() }
    }

    private fun scheduleAlarms(timings: Timings) {
        val prayers = listOf("الفجر" to timings.Fajr, "الظهر" to timings.Dhuhr, "العصر" to timings.Asr, "المغرب" to timings.Maghrib, "العشاء" to timings.Isha)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        prayers.forEach { (name, time) ->
            val triggerTime = getTime(time)
            if (triggerTime > System.currentTimeMillis()) {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    action = "com.azanapp.PRAYER_ALARM"
                    putExtra("prayer_name", name)
                    putExtra("prayer_time", time.substring(0, 5))
                }
                val pi = PendingIntent.getBroadcast(context, name.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                try { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi) }
                catch (e: SecurityException) { am.set(AlarmManager.RTC_WAKEUP, triggerTime, pi) }
            }
        }
    }

    private fun getTime(t: String): Long {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val cal = Calendar.getInstance()
            val parsed = sdf.parse(t.substring(0, 5)) ?: return 0L
            val tmp = Calendar.getInstance().apply { time = parsed }
            cal.set(Calendar.HOUR_OF_DAY, tmp.get(Calendar.HOUR_OF_DAY))
            cal.set(Calendar.MINUTE, tmp.get(Calendar.MINUTE))
            cal.set(Calendar.SECOND, 0)
            cal.timeInMillis
        } catch (e: Exception) { 0L }
    }

    private fun saveTimings(t: Timings) {
        context.getSharedPreferences("azan_prefs", Context.MODE_PRIVATE).edit()
            .putString("fajr", t.Fajr.substring(0, 5)).putString("dhuhr", t.Dhuhr.substring(0, 5))
            .putString("asr", t.Asr.substring(0, 5)).putString("maghrib", t.Maghrib.substring(0, 5))
            .putString("isha", t.Isha.substring(0, 5)).putString("sunrise", t.Sunrise.substring(0, 5)).apply()
    }
}
