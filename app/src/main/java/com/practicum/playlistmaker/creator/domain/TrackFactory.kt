package com.practicum.playlistmaker.creator.domain

import com.practicum.playlistmaker.core.models.Track

class TrackFactory {
    fun createTrack(
        trackName: String,
        artistName: String,
        trackTimeMillis: Long? = null,
        artworkUrl100: String? = null,
        releaseDate: String? = null,
        collectionName: String? = null,
        primaryGenreName: String? = null,
        country: String? = null,
        previewUrl: String? = null,
        addedDate: Long? = null
    ): Track {
        val finalAddedDate = addedDate ?: System.currentTimeMillis()
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
            previewUrl = previewUrl,
            addedDate = finalAddedDate
        )
    }
    private fun generateTrackId(
        trackName: String,
        artistName: String,
        collectionName: String?
    ): String {
        return "$trackName:$artistName:${collectionName ?: ""}".lowercase()
    }
}