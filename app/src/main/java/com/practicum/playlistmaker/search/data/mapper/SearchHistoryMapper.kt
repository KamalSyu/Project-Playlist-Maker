package com.practicum.playlistmaker.search.data.mapper

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.data.dto.SearchHistoryDTO

class SearchHistoryMapper(
    private val trackMapper: TrackMapper
) {
    fun toDto(tracks: List<Track>): SearchHistoryDTO {
        return SearchHistoryDTO(tracks = tracks.map { trackMapper.toDto(it) })
    }
    fun fromDto(dto: SearchHistoryDTO): List<Track> {
        return dto.tracks.map { trackMapper.toDomain(it) }
    }
}
