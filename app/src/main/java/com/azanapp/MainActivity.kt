package com.azanapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var tvCity: TextView
    private lateinit var tvStatus: TextView
    private lateinit var locationHelper: LocationHelper

    private val locationLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) initApp()
        else tvStatus.text = "⚠️ يرجى السماح بالوصول إلى الموقع"
    }

    private val notifLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvCity = findViewById(R.id.tvCity)
        tvStatus = findViewById(R.id.tvStatus)
        locationHelper = LocationHelper(this)
        NotificationHelper(this).createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

        if (!locationHelper.hasLocationPermission())
            locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        else initApp()

        findViewById<Button>(R.id.btnShowTimes).setOnClickListener { startActivity(Intent(this, PrayerTimesActivity::class.java)) }
        findViewById<Button>(R.id.btnRefresh).setOnClickListener { initApp() }
    }

    private fun initApp() {
        tvStatus.text = "⏳ جاري تحديد موقعك..."
        lifecycleScope.launch {
            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                tvCity.text = "📍 ${locationHelper.getCityName(loc.first, loc.second)}"
                scheduleWork()
                triggerNow()
                tvStatus.text = "✅ التنبيهات مُفعّلة"
            } else tvStatus.text = "❌ تعذّر تحديد الموقع. فعّل GPS"
        }
    }

    private fun scheduleWork() {
        val work = PeriodicWorkRequestBuilder<PrayerWorker>(24, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("prayer_daily", ExistingPeriodicWorkPolicy.KEEP, work)
    }

    private fun triggerNow() {
        val work = OneTimeWorkRequestBuilder<PrayerWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        WorkManager.getInstance(this).enqueue(work)
    }
}
