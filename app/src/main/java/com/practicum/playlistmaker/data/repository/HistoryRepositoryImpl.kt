package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.repository.HistoryRepository

class HistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : HistoryRepository {

    override suspend fun addTrack(track: Track) {
        with(sharedPreferences.edit()) {
            putString(track.trackId.toString(), Gson().toJson(track))
            apply()
        }
    }

    override suspend fun getHistory(): List<Track> {
        return sharedPreferences.all.mapNotNull { entry ->
            try {
                Gson().fromJson(entry.value.toString(), Track::class.java)
            } catch (e: Exception) {
                null
            }
        }.filterNotNull()
    }

    override suspend fun clearHistory() {
        sharedPreferences.edit().clear().apply()
    }
}

