package com.practicum.playlistmaker.player.data.mapper

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.data.db.FavoriteTrackEntity

class TrackDbMapper {

    fun toEntity(track: Track): FavoriteTrackEntity {
        return FavoriteTrackEntity(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            artworkUrl100 = track.artworkUrl100,
            previewUrl = track.previewUrl,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            trackTimeMillis = track.trackTimeMillis,
            addedAt = track.addedAt
        )
    }

    fun toDomain(entity: FavoriteTrackEntity): Track {
        return Track(
            trackId = entity.trackId,
            trackName = entity.trackName,
            artistName = entity.artistName,
            artworkUrl100 = entity.artworkUrl100,
            previewUrl = entity.previewUrl,
            collectionName = entity.collectionName,
            releaseDate = entity.releaseDate,
            primaryGenreName = entity.primaryGenreName,
            country = entity.country,
            trackTimeMillis = entity.trackTimeMillis,
            addedAt = entity.addedAt
        )
    }
}