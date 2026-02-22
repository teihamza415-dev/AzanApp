package com.azanapp

data class PrayerTimesResponse(val code: Int, val status: String, val data: PrayerData)
data class PrayerData(val timings: Timings, val date: DateInfo)
data class Timings(val Fajr: String, val Sunrise: String, val Dhuhr: String, val Asr: String, val Maghrib: String, val Isha: String)
data class DateInfo(val readable: String, val hijri: HijriDate)
data class HijriDate(val date: String, val month: HijriMonth, val year: String)
data class HijriMonth(val en: String, val ar: String)
