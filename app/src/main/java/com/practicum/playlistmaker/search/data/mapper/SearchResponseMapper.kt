package com.practicum.playlistmaker.search.data.mapper

import com.practicum.playlistmaker.search.data.dto.SearchResponseDTO
import com.practicum.playlistmaker.search.domain.model.SearchResponse
import javax.inject.Inject

class SearchResponseMapper @Inject constructor(
    private val trackMapper: TrackMapper
) {

    fun toDomain(dto: SearchResponseDTO): SearchResponse {
        return SearchResponse(
            resultCount = dto.resultCount,
            results = dto.results.map { trackMapper.toDomain(it) }
        )
    }
}
