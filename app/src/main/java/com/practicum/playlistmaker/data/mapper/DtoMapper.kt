package com.practicum.playlistmaker.data.mapper

import com.practicum.playlistmaker.data.dto.SearchResponseDTO
import com.practicum.playlistmaker.data.dto.TrackDTO
import com.practicum.playlistmaker.domain.factory.TrackFactory
import com.practicum.playlistmaker.domain.model.SearchResponse
import com.practicum.playlistmaker.domain.model.Track
import javax.inject.Inject

class DtoMapper @Inject constructor(
    private val trackFactory: TrackFactory
) {

    fun toDomain(trackDto: TrackDTO): Track {
        return trackFactory.createTrack(
            trackName = trackDto.trackName,
            artistName = trackDto.artistName,
            trackTimeMillis = trackDto.trackTimeMillis,
            artworkUrl100 = trackDto.artworkUrl100,
            releaseDate = trackDto.releaseDate,
            collectionName = trackDto.collectionName,
            primaryGenreName = trackDto.primaryGenreName,
            country = trackDto.country,
            previewUrl = trackDto.previewUrl
        )
    }

    fun toDomain(searchResponseDto: SearchResponseDTO): SearchResponse {
        return SearchResponse(
            resultCount = searchResponseDto.resultCount,
            results = searchResponseDto.results.map { toDomain(it) }  // Используем toDomain(TrackDTO)
        )
    }
}

