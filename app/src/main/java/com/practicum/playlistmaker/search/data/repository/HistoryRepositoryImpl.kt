package com.practicum.playlistmaker.search.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : HistoryRepository {

    companion object {
        private const val HISTORY_KEY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }

    private val historyKey = HISTORY_KEY
    private val _history = MutableStateFlow<List<Track>>(emptyList())

    override fun getHistory(): Flow<List<Track>> = _history.asStateFlow()

    override suspend fun addTrack(track: Track) {
        val current = _history.value.toMutableList()
        current.removeAll { it.trackId == track.trackId }
        current.add(0, track)
        val limited = current.take(MAX_HISTORY_SIZE)
        _history.value = limited

        val json = gson.toJson(limited)
        sharedPreferences.edit().putString(historyKey, json).apply()
    }

    override suspend fun clearHistory() {
        _history.value = emptyList()
        sharedPreferences.edit().remove(historyKey).apply()
    }

    private fun loadHistory() {
        val json = sharedPreferences.getString(historyKey, null)
        val tracks = if (json != null) {
            try {
                gson.fromJson(json, Array<Track>::class.java).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        _history.value = tracks
    }
}