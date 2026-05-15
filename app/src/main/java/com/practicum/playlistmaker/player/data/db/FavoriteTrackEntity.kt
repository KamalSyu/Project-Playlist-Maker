package com.practicum.playlistmaker.player.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_tracks")
data class FavoriteTrackEntity(
    @PrimaryKey val trackId: String,
    val trackName: String,
    val artistName: String,
    val artworkUrl100: String?,
    val releaseDate: String?,
    val collectionName: String?,
    val primaryGenreName: String?,
    val country: String?,
    val trackTimeMillis: Long?,
    val previewUrl: String?,
    val addedDate: Long
)