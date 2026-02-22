package com.azanapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

class PrayerTimesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prayer_times)
        loadTimes()
    }

    private fun loadTimes() {
        val helper = LocationHelper(this)
        lifecycleScope.launch {
            val loc = helper.getCurrentLocation() ?: helper.getSavedLocation() ?: return@launch
            findViewById<TextView>(R.id.tvCityName).text = "📍 ${helper.getCityName(loc.first, loc.second)}"
            try {
                val resp = PrayerApiService.create().getPrayerTimes(loc.first, loc.second)
                if (resp.isSuccessful) {
                    val t = resp.body()?.data?.timings ?: return@launch
                    setTime(R.id.timeFajr, t.Fajr); setTime(R.id.timeSunrise, t.Sunrise)
                    setTime(R.id.timeDhuhr, t.Dhuhr); setTime(R.id.timeAsr, t.Asr)
                    setTime(R.id.timeMaghrib, t.Maghrib); setTime(R.id.timeIsha, t.Isha)
                    showNext(t)
                }
            } catch (e: Exception) { loadSaved() }
        }
    }

    private fun setTime(id: Int, time: String) {
        findViewById<TextView>(id).text = time.substring(0, 5)
    }

    private fun showNext(t: Timings) {
        val now = Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
        val prayers = listOf("الفجر" to t.Fajr, "الظهر" to t.Dhuhr, "العصر" to t.Asr, "المغرب" to t.Maghrib, "العشاء" to t.Isha)
        val next = prayers.firstOrNull { (_, time) -> val p = time.split(":"); p[0].toInt() * 60 + p[1].toInt() > now }
        next?.let { findViewById<TextView>(R.id.tvNextPrayer).text = "⏰ الصلاة القادمة: ${it.first} الساعة ${it.second.substring(0,5)}" }
    }

    private fun loadSaved() {
        val p = getSharedPreferences("azan_prefs", MODE_PRIVATE)
        mapOf(R.id.timeFajr to "fajr", R.id.timeDhuhr to "dhuhr", R.id.timeAsr to "asr", R.id.timeMaghrib to "maghrib", R.id.timeIsha to "isha", R.id.timeSunrise to "sunrise")
            .forEach { (id, key) -> findViewById<TextView>(id).text = p.getString(key, "--:--") }
    }
}
