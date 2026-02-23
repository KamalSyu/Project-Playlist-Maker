package com.practicum.playlistmaker.player.data.mapper

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack

class TrackParcelableMapper {
    fun toDomain(parcelable: ParcelableTrack): Track {
        return Track(
            trackId = parcelable.trackId,
            trackName = parcelable.trackName,
            artistName = parcelable.artistName,
            collectionName = parcelable.collectionName,
            artworkUrl100 = parcelable.artworkUrl100,
            previewUrl = parcelable.previewUrl,
            releaseDate = parcelable.releaseDate,
            primaryGenreName = parcelable.primaryGenreName,
            country = parcelable.country,
            trackTimeMillis = parcelable.trackTimeMillis
        )
    }
}