package com.practicum.playlistmaker.data.dto

data class TrackDTO(
    var trackName: String,
    var artistName: String,
    var trackTimeMillis: Long,
    var artworkUrl100: String?,
    var releaseDate: String?,
    var collectionName: String?,
    var primaryGenreName: String?,
    var country: String?,
    var previewUrl: String?
)