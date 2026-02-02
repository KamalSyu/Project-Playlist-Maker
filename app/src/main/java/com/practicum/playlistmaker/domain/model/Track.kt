package com.practicum.playlistmaker.domain.model

data class Track(
    val trackId: String,
    var trackName: String,
    var artistName: String,
    var trackTimeMillis: Long?,
    var artworkUrl100: String?,
    var releaseDate: String?,
    var collectionName: String?,
    var primaryGenreName: String?,
    var country: String?,
    var previewUrl: String?
)  {

    fun getHighQualityArtworkUrl(): String? {
        return artworkUrl100?.replace("100x100bb.jpg", "512x512bb.jpg")
    }

}
