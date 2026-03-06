package com.practicum.playlistmaker.search.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.domain.repository.HistoryRepository
import com.practicum.playlistmaker.search.data.dto.SearchHistoryDTO
import com.practicum.playlistmaker.search.data.mapper.SearchHistoryMapper
import com.practicum.playlistmaker.core.constants.Constants
import kotlin.collections.plus

/**
 * Реализация репозитория для работы с историей поиска треков.
 * Сохраняет и извлекает историю из SharedPreferences с использованием GSON для сериализации.
 *
 * @param sharedPreferences хранилище настроек для сохранения истории
 * @param gson конвертер объектов в JSON и обратно
 * @param searchHistoryMapper маппер для преобразования между доменными моделями и DTO
 */
class HistoryRepositoryImpl (
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
    private val searchHistoryMapper: SearchHistoryMapper
) : HistoryRepository {

    /**
     * Добавляет трек в начало истории поиска.
     * - удаляет дубликаты по trackId;
     * - ограничивает размер истории MAX_HISTORY_SIZE;
     * - сохраняет обновлённую историю в SharedPreferences.
     *
     * @param track трек для добавления в историю
     */
    override suspend fun addTrack(track: Track) {
        val currentHistory = getHistory()
        val updatedHistory = (listOf(track) + currentHistory)
            .distinctBy { it.trackId }
            .take(Constants.Companion.MAX_HISTORY_SIZE)

        val dto = searchHistoryMapper.toDto(updatedHistory)
        sharedPreferences.edit()
            .putString(Constants.Companion.HISTORY_KEY, gson.toJson(dto))
            .apply()
    }

    /**
     * Получает историю поиска из SharedPreferences.
     * - если история отсутствует (null), возвращает пустой список;
     * - при ошибке парсинга JSON возвращает пустой список.
     *
     * @return список треков в порядке добавления (последний — первый)
     */
    override suspend fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(Constants.Companion.HISTORY_KEY, null)
        if (json == null) return emptyList()

        try {
            val dto: SearchHistoryDTO = gson.fromJson(json, SearchHistoryDTO::class.java)
            return searchHistoryMapper.fromDto(dto)
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * Очищает всю историю поиска, удаляя запись из SharedPreferences.
     */
    override suspend fun clearHistory() {
        sharedPreferences.edit()
            .remove(Constants.Companion.HISTORY_KEY)
            .apply()
    }
}
