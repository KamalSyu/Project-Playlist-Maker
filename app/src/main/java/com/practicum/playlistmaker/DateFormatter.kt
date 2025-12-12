package com.practicum.playlistmaker

import java.text.SimpleDateFormat
import java.util.Locale

class DateFormatter {
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    private val outputFormat = SimpleDateFormat("yyyy", Locale.getDefault())

    fun formatReleaseDate(releaseDateString: String?): String {
        return if (releaseDateString != null) {
            try {
                val date = inputFormat.parse(releaseDateString)
                outputFormat.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
                "Ошибка при парсинге даты"
            }
        } else {
            "-"
        }
    }
}