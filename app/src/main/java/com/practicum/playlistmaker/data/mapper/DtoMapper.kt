package com.practicum.playlistmaker.data.mapper

import com.practicum.playlistmaker.data.dto.SearchHistoryDTO
import com.practicum.playlistmaker.data.dto.SearchResponseDTO
import com.practicum.playlistmaker.data.dto.ThemeSettingsDTO
import com.practicum.playlistmaker.data.dto.TrackDTO
import com.practicum.playlistmaker.domain.factory.TrackFactory
import com.practicum.playlistmaker.domain.model.SearchResponse
import com.practicum.playlistmaker.domain.model.ThemeSettings
import com.practicum.playlistmaker.domain.model.Track
import javax.inject.Inject

class DtoMapper @Inject constructor(
    private val trackFactory: TrackFactory
) {

    // === ПРЕОБРАЗОВАНИЕ DTO → DOMAIN ===


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

    // === ПРЕОБРАЗОВАНИЕ DOMAIN → DTO (для сохранения в Data-слой) ===


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



    fun toDto(searchResponse: SearchResponse): SearchResponseDTO {
        return SearchResponseDTO(
            resultCount = searchResponse.resultCount,
            results = searchResponse.results.map { toDto(it) }
        )
    }

    // === ДОПОЛНИТЕЛЬНО: ДЛЯ ИСТОРИИ ПОИСКА ===
    // Преобразует список треков (Domain) в DTO для хранения
    fun toSearchHistoryDto(tracks: List<Track>): SearchHistoryDTO {
        return SearchHistoryDTO(
            tracks = tracks.map { toDto(it) }  // Используем toDto(Track)
        )
    }

    // Преобразует DTO истории в список треков (Domain)
    fun fromSearchHistoryDto(dto: SearchHistoryDTO): List<Track> {
        return dto.tracks.map { toDomain(it) }  // Используем toDomain(TrackDTO)
    }

    fun toDto(settings: ThemeSettings): ThemeSettingsDTO {
        return ThemeSettingsDTO(isDarkTheme = settings.isDarkTheme)
    }
    // В классе DtoMapper
    fun fromDto(dto: ThemeSettingsDTO): ThemeSettings {
        return ThemeSettings(
            isDarkTheme = dto.isDarkTheme
        )
    }


}

