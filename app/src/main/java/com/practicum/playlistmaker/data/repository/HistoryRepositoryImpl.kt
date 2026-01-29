package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.repository.HistoryRepository

class HistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : HistoryRepository {

    override suspend fun addTrack(track: Track) {
        val history = getHistory().toMutableList()
        history.removeIf { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > 10) {
            history.subList(10, history.size).clear()
        }
        sharedPreferences.edit()
            .putString("history", Gson().toJson(history))
            .apply()
    }

    override suspend fun getHistory(): List<Track> {
        val json = sharedPreferences.getString("history", "[]")
        return Gson().fromJson(json, Array<Track>::class.java).toList()
    }

    override suspend fun clearHistory() {
        sharedPreferences.edit().clear().apply()
    }
}

