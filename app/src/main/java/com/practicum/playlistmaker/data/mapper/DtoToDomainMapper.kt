package com.practicum.playlistmaker.data.mapper

import com.practicum.playlistmaker.data.dto.SearchResponseDTO
import com.practicum.playlistmaker.data.dto.TrackDTO
import com.practicum.playlistmaker.domain.model.SearchResponse
import com.practicum.playlistmaker.domain.model.Track

fun SearchResponseDTO.toDomain(): SearchResponse {
    return SearchResponse(
        resultCount = this.resultCount,
        results = this.results.map { it.toDomain() }
    )
}
fun TrackDTO.toDomain(): Track {
    return Track(
        trackId = 0,
        trackName = this.trackName,
        artistName = this.artistName,
        trackTimeMillis = this.trackTimeMillis,
        artworkUrl100 = this.artworkUrl100,
        releaseDate = this.releaseDate,
        collectionName = this.collectionName,
        primaryGenreName = this.primaryGenreName,
        country = this.country,
        previewUrl = this.previewUrl
    )
}