package com.practicum.playlistmaker.search.data.mapper

import com.practicum.playlistmaker.search.data.dto.SearchHistoryDTO
import com.practicum.playlistmaker.search.data.dto.SearchResponseDTO
import com.practicum.playlistmaker.settings.data.dto.ThemeSettingsDTO
import com.practicum.playlistmaker.search.data.dto.TrackDTO
import com.practicum.playlistmaker.creator.domain.TrackFactory
import com.practicum.playlistmaker.search.domain.model.SearchResponse
import com.practicum.playlistmaker.settings.domain.model.ThemeSettings
import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack
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
            results = searchResponseDto.results.map { toDomain(it) }
        )
    }

    fun toDomain(parcelableTrack: ParcelableTrack): Track {
        return trackFactory.createTrack(
            trackName = parcelableTrack.trackName,
            artistName = parcelableTrack.artistName,
            trackTimeMillis = parcelableTrack.trackTimeMillis,
            artworkUrl100 = parcelableTrack.artworkUrl100,
            releaseDate = parcelableTrack.releaseDate,
            collectionName = parcelableTrack.collectionName,
            primaryGenreName = parcelableTrack.primaryGenreName,
            country = parcelableTrack.country,
            previewUrl = parcelableTrack.previewUrl,
        )
    }

    fun toDto(track: Track): TrackDTO {
        return TrackDTO(
            trackName = track.trackName ?: "",
            artistName = track.artistName ?: "",
            trackTimeMillis = track.trackTimeMillis ?: 0L,
            artworkUrl100 = track.artworkUrl100,
            releaseDate = track.releaseDate,
            collectionName = track.collectionName,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl
        )
    }

    fun toParcelableTrack(track: Track): ParcelableTrack {
        return ParcelableTrack(
            trackId = track.trackId,
            trackName = track.trackName ?: "",
            artistName = track.artistName ?: "",
            trackTimeMillis = track.trackTimeMillis ?: 0L,
            artworkUrl100 = track.artworkUrl100,
            releaseDate = track.releaseDate,
            collectionName = track.collectionName,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl
        )
    }

    fun toSearchHistoryDto(tracks: List<Track>): SearchHistoryDTO {
        return SearchHistoryDTO(
            tracks = tracks.map { toDto(it) }
        )
    }

    fun fromSearchHistoryDto(dto: SearchHistoryDTO): List<Track> {
        return dto.tracks.map { toDomain(it) }
    }

    fun toDto(settings: ThemeSettings): ThemeSettingsDTO {
        return ThemeSettingsDTO(isDarkTheme = settings.isDarkTheme)
    }

    fun fromDto(dto: ThemeSettingsDTO): ThemeSettings {
        return ThemeSettings(isDarkTheme = dto.isDarkTheme)
    }
}


