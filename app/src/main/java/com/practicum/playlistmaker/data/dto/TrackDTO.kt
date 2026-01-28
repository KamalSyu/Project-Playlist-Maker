package com.practicum.playlistmaker.data.dto

data class TrackDTO(
    var trackName: String,
    var artistName: String,
    var trackTimeMillis: Long,
    var artworkUrl100: String,
    var releaseDate: String? = null,
    var collectionName: String? = null,
    var primaryGenreName: String? = null,
    var country: String? = null,
    var previewUrl: String? = null
)