package com.practicum.playlistmaker.domain.model

data class SearchResponse(
    val resultCount: Int,
    val results: List<Track>
)
