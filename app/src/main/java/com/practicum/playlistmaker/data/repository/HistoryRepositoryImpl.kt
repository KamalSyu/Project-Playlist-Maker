package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.data.dto.SearchHistoryDTO
import com.practicum.playlistmaker.data.mapper.DtoMapper
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.repository.HistoryRepository
import com.practicum.playlistmaker.utils.Constants.Companion.HISTORY_KEY
import com.practicum.playlistmaker.utils.Constants.Companion.MAX_HISTORY_SIZE
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
    private val dtoMapper: DtoMapper
) : HistoryRepository {

    override suspend fun addTrack(track: Track) {
        val currentHistory = getHistory()
        val updatedHistory = (listOf(track) + currentHistory)
            .distinctBy { it.trackId }
            .take(MAX_HISTORY_SIZE)

        val dto = dtoMapper.toSearchHistoryDto(updatedHistory)
        sharedPreferences.edit()
            .putString(HISTORY_KEY, gson.toJson(dto))
            .apply()
    }

    override suspend fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null)
        if (json == null) return emptyList()

        try {
            val dto: SearchHistoryDTO = gson.fromJson(json, SearchHistoryDTO::class.java)
            return dtoMapper.fromSearchHistoryDto(dto)
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    override suspend fun clearHistory() { // Очищает историю поиска
        sharedPreferences.edit()
            .remove(HISTORY_KEY)
            .apply()
    }
}
