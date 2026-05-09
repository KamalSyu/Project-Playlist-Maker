package com.practicum.playlistmaker.core.models

import com.practicum.playlistmaker.search.ui.parcel.ParcelableTrack


fun Track.toParcelable(): ParcelableTrack {
    return ParcelableTrack(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        releaseDate = releaseDate,
        collectionName = collectionName,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl,
        addedDate = addedDate
    )
}
