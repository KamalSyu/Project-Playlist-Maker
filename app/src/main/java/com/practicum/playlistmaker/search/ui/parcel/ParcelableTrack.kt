package com.practicum.playlistmaker.search.ui.parcel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.practicum.playlistmaker.core.models.Track

@Parcelize
data class ParcelableTrack(
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
) : Parcelable {
}

fun Track.toParcelable() = ParcelableTrack(
    trackId = this.trackId,
    trackName = this.trackName,
    artistName = this.artistName,
    trackTimeMillis = this.trackTimeMillis,
    artworkUrl100 = this.artworkUrl100,
    releaseDate = this.releaseDate,
    collectionName = this.collectionName,
    primaryGenreName = this.primaryGenreName,
    country = this.country,
    previewUrl = this.previewUrl
)