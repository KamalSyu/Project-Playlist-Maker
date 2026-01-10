package com.practicum.playlistmaker

import Track
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val context: Context) {

    private val PREFS_NAME = "search_history_pref"
    private val HISTORY_KEY = "search_history"
    private val sizeHistory = 10


    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Получить список истории
    fun getHistory(): MutableList<Track> {
        val json = prefs.getString(HISTORY_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Track>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    // Сохранить список истории
    private fun saveHistory(history: MutableList<Track>) {
        val json = gson.toJson(history)
        prefs.edit().putString(HISTORY_KEY, json).apply()
    }

    // Добавить трек в историю
    fun addTrack(track: Track) {
        val history = getHistory()
        // Проверка уникальности по trackId
        val existsIndex = history.indexOfFirst { it.trackId == track.trackId }
        if (existsIndex != -1) {
            // Удаляем старый экземпляр
            history.removeAt(existsIndex)
        }
        // Добавляем в начало
        history.add(0, track)

        // Ограничиваем количество треков до 10
        if (history.size > sizeHistory) {
            history.removeAt(history.size - 1)
        }
        saveHistory(history)
    }

    // Очистить историю
    fun clearHistory() {
        prefs.edit().remove(HISTORY_KEY).apply()
    }
}
