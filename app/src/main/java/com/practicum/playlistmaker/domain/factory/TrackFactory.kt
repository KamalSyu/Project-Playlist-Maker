package com.practicum.playlistmaker.domain.factory

import com.practicum.playlistmaker.domain.model.Track


class TrackFactory {

    /**
     * Создаёт экземпляр Track с автоматически сгенерированным trackId.
     */
    fun createTrack(
        trackName: String,
        artistName: String,
        trackTimeMillis: Long? = null,
        artworkUrl100: String? = null,
        releaseDate: String? = null,
        collectionName: String? = null,
        primaryGenreName: String? = null,
        country: String? = null,
        previewUrl: String? = null
    ): Track {
        val trackId = generateTrackId(trackName, artistName, collectionName)
        return Track(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl100,
            releaseDate = releaseDate,
            collectionName = collectionName,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl
        )
    }

    /**
     * Генерирует уникальный идентификатор трека на основе ключевых полей.
     * Формат: "$trackName:$artistName:${collectionName?:""}".lowercase()
     */
    private fun generateTrackId(
        trackName: String,
        artistName: String,
        collectionName: String?
    ): String {
        return "$trackName:$artistName:${collectionName ?: ""}".lowercase()
    }
}
