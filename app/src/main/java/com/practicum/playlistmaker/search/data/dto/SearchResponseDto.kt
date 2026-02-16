package com.practicum.playlistmaker.search.data.dto

import com.practicum.playlistmaker.search.data.dto.TrackDTO

data class SearchResponseDTO(
    val resultCount: Int,
    val results: List<TrackDTO>
)