package com.practicum.playlistmaker.search.data.dto

data class TrackDTO(
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String?,
    val releaseDate: String?,
    val collectionName: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
)