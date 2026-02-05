package com.practicum.playlistmaker.domain.repository

import com.practicum.playlistmaker.domain.model.SearchResponse
// Назначение: реализация бизнес‑правил и сценариев использования.

// Поиск треков через iTunes API
interface ItunesRepository {
    suspend fun search(query: String): SearchResponse
}
