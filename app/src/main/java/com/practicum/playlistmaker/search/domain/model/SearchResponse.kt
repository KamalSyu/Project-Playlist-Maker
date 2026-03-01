package com.practicum.playlistmaker.search.domain.model

import com.practicum.playlistmaker.core.models.Track

data class SearchResponse(
    val resultCount: Int,
    val results: List<Track>
)