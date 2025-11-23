package com.practicum.playlistmaker

import Track

data class SearchResponse(
    val resultCount: Int,
    val results: List<Track>
)