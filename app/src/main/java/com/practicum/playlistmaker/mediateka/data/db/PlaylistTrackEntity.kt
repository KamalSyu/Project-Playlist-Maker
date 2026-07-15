package com.practicum.playlistmaker.mediateka.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.practicum.playlistmaker.core.models.Track

@Entity(
    tableName = "playlist_tracks",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playlistId"]), Index(value = ["trackId"])]
)
data class PlaylistTrackEntity(
    @PrimaryKey
    val id: Long,
    val playlistId: Long,
    val trackId: String,
    val title: String,
    val artist: String,
    val duration: Int,
    val addedAt: Long
)
fun PlaylistTrackEntity.toTrack(): Track = Track(
    trackId = this.trackId,
    trackName = this.title,
    artistName = this.artist,
    artworkUrl100 = null,                 // у тебя в Entity нет поля для обложки трека — ставим null
    trackTimeMillis = (this.duration * 1_000).toLong(),
    releaseDate = null,
    collectionName = null,
    primaryGenreName = null,
    country = null,
    previewUrl = null,
    addedDate = this.addedAt,
    isFavorite = false
)
