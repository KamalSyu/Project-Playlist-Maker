package com.practicum.playlistmaker.core.models

data class Track(
    val trackId: String,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val releaseDate: String?,
    val collectionName: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
)  {

    fun getHighQualityArtworkUrl(): String? {
        return artworkUrl100?.replace("100x100bb.jpg", "512x512bb.jpg")
    }

}