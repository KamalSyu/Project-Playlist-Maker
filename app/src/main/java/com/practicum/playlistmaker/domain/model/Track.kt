
package com.practicum.playlistmaker.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val trackId: Int,           // локальный ID (не из API!)
    var trackName: String,
    var artistName: String,
    var trackTimeMillis: Long?,     // nullable — может отсутствовать в ответе
    var artworkUrl100: String?,   // nullable — может быть пустым
    var releaseDate: String?,      // nullable
    var collectionName: String?,
    var primaryGenreName: String?,
    var country: String?,
    var previewUrl: String?        // nullable — превью может отсутствовать
) : Parcelable {

    fun getHighQualityArtworkUrl(): String {
        return artworkUrl100?.replaceAfterLast("/", "512x512bb.jpg")
            ?: "https://default-image.com/placeholder.jpg"
    }
}
