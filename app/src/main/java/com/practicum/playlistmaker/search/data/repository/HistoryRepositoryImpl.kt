package com.practicum.playlistmaker.search.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.search.data.dto.SearchHistoryDTO
import com.practicum.playlistmaker.search.data.mapper.DtoMapper
import com.practicum.playlistmaker.core.constants.Constants
import javax.inject.Inject
import kotlin.collections.plus

class HistoryRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
    private val dtoMapper: DtoMapper
) : HistoryRepository {

    override suspend fun addTrack(track: Track) {
        val currentHistory = getHistory()
        val updatedHistory = (listOf(track) + currentHistory)
            .distinctBy { it.trackId }
            .take(Constants.Companion.MAX_HISTORY_SIZE)

        val dto = dtoMapper.toSearchHistoryDto(updatedHistory)
        sharedPreferences.edit()
            .putString(Constants.Companion.HISTORY_KEY, gson.toJson(dto))
            .apply()
    }

    override suspend fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(Constants.Companion.HISTORY_KEY, null)
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
            .remove(Constants.Companion.HISTORY_KEY)
            .apply()
    }
}