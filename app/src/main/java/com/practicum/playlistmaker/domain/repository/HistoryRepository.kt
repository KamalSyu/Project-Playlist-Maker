package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.model.Track
// Назначение: реализация бизнес‑правил и сценариев использования.

// История поиска
interface HistoryRepository {
    suspend fun addTrack(track: Track)   // Добавить трек в историю
    suspend fun getHistory(): List<Track> // Получить историю
    suspend fun clearHistory()            // Очистить историю
}