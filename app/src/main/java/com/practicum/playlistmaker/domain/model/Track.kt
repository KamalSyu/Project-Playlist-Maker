
package com.practicum.playlistmaker.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable {

    constructor(
        trackName: String,
        artistName: String,
        trackTimeMillis: Long? = null,
        artworkUrl100: String? = null,
        releaseDate: String? = null,
        collectionName: String? = null,
        primaryGenreName: String? = null,
        country: String? = null,
        previewUrl: String? = null
    ) : this(
        trackId = "$trackName:$artistName:${collectionName ?: ""}".lowercase(),
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

    fun getHighQualityArtworkUrl(): String? {
        return artworkUrl100?.replace("100x100bb.jpg", "512x512bb.jpg")
    }

}
