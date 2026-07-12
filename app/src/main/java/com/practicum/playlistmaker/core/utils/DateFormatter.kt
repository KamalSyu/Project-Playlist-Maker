package com.practicum.playlistmaker.core.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DateFormatter {
    private val inputFormat: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    private val outputFormat: SimpleDateFormat by lazy {
        SimpleDateFormat("yyyy", Locale.US)
    }
    fun formatReleaseDate(releaseDateString: String?): String {
        if (releaseDateString == null) return "-"
        return try {
            val date = inputFormat.parse(releaseDateString) ?: return "-"
            outputFormat.format(date)
        } catch (e: Exception) {
            Log.w("DateFormatter", "Failed to parse release date: $releaseDateString", e)
            "-"
        }
    }
}
