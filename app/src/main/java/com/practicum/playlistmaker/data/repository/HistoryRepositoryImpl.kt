package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.data.dto.SearchHistoryDTO
import com.practicum.playlistmaker.data.mapper.DtoMapper
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.repository.HistoryRepository
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
    private val dtoMapper: DtoMapper
) : HistoryRepository {

    companion object {
        private const val HISTORY_KEY = "search_history"
    }

    override suspend fun addTrack(track: Track) {
        val currentHistory = getHistory()
        val updatedHistory = (listOf(track) + currentHistory)
            .distinctBy { it.trackId }
            .take(10) // Ограничение до 10 последних треков

        // Преобразуем в DTO и сохраняем
        val dto = dtoMapper.toSearchHistoryDto(updatedHistory)
        sharedPreferences.edit()
            .putString(HISTORY_KEY, gson.toJson(dto))
            .apply()
    }

    override suspend fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null)
        if (json == null) return emptyList()

        try {
            // Читаем DTO из JSON
            val dto: SearchHistoryDTO = gson.fromJson(json, SearchHistoryDTO::class.java)
            // Преобразуем DTO → List<Track>
            return dtoMapper.fromSearchHistoryDto(dto)
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList() // Обработка ошибок (некорректный JSON и т.п.)
        }
    }

    override suspend fun clearHistory() {
        sharedPreferences.edit()
            .remove(HISTORY_KEY)
            .apply()
    }
}
