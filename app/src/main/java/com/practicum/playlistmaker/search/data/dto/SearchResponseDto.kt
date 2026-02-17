package com.practicum.playlistmaker.search.data.dto

data class SearchResponseDTO(
    val resultCount: Int,
    val results: List<TrackDTO>
)