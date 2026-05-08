package com.practicum.playlistmaker.core.models.parcel

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
    companion object {
        fun toDomain(parcelableTrack: ParcelableTrack): Track {
            return Track(
                trackId = parcelableTrack.trackId,
                trackName = parcelableTrack.trackName,
                artistName = parcelableTrack.artistName,
                trackTimeMillis = parcelableTrack.trackTimeMillis,
                artworkUrl100 = parcelableTrack.artworkUrl100,
                releaseDate = parcelableTrack.releaseDate,
                collectionName = parcelableTrack.collectionName,
                primaryGenreName = parcelableTrack.primaryGenreName,
                country = parcelableTrack.country,
                previewUrl = parcelableTrack.previewUrl
            )
        }
    }
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