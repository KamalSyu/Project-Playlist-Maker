package com.practicum.playlistmaker.search.data.mapper

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.creator.domain.TrackFactory
import com.practicum.playlistmaker.search.data.dto.TrackDTO
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
import javax.inject.Inject

class TrackMapper @Inject constructor(
    private val trackFactory: TrackFactory
) {

    fun toDomain(dto: TrackDTO): Track {
        return trackFactory.createTrack(
            trackName = dto.trackName,
            artistName = dto.artistName,
            trackTimeMillis = dto.trackTimeMillis,
            artworkUrl100 = dto.artworkUrl100,
            releaseDate = dto.releaseDate,
            collectionName = dto.collectionName,
            primaryGenreName = dto.primaryGenreName,
            country = dto.country,
            previewUrl = dto.previewUrl
        )
    }

    fun toDto(domain: Track): TrackDTO {
        return TrackDTO(
            trackName = domain.trackName ?: "",
            artistName = domain.artistName ?: "",
            trackTimeMillis = domain.trackTimeMillis ?: 0L,
            artworkUrl100 = domain.artworkUrl100,
            releaseDate = domain.releaseDate,
            collectionName = domain.collectionName,
            primaryGenreName = domain.primaryGenreName,
            country = domain.country,
            previewUrl = domain.previewUrl
        )
    }

    fun toParcelable(domain: Track): ParcelableTrack {
        return ParcelableTrack(
            trackId = domain.trackId,
            trackName = domain.trackName ?: "",
            artistName = domain.artistName ?: "",
            trackTimeMillis = domain.trackTimeMillis ?: 0L,
            artworkUrl100 = domain.artworkUrl100,
            releaseDate = domain.releaseDate,
            collectionName = domain.collectionName,
            primaryGenreName = domain.primaryGenreName,
            country = domain.country,
            previewUrl = domain.previewUrl
        )
    }
}
