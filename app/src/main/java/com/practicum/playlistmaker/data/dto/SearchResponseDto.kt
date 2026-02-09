package com.practicum.playlistmaker.data.dto

data class SearchResponseDTO(
    val resultCount: Int,
    val results: List<TrackDTO>
)