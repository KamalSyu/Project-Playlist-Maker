package com.practicum.playlistmaker.domain.model

// Контейнер для данных, который приходит из API iTunes после поиска треков.

data class SearchResponse(
    val resultCount: Int,          // Количество найденных треков
    val results: List<Track>     // Список найденных треков
)
