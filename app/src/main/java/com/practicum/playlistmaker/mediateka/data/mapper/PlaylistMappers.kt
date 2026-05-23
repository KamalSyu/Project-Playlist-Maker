package com.practicum.playlistmaker.mediateka.data.mapper

import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistEntity

fun PlaylistEntity.toDomain(): Playlist {
    return Playlist(
        id = this.id.toString(),
        name = this.name,
        description = this.description,
        coverPath = this.coverPath,
        trackIds = this.trackIds,
        trackCount = this.trackCount,
        createdAt = this.createdAt
    )
}

fun Playlist.toEntity(): PlaylistEntity {
    val id = this.id.toLongOrNull()
        ?: throw IllegalArgumentException("Invalid playlist ID: ${this.id}")
    return PlaylistEntity(
        id = id,
        name = this.name,
        description = this.description,
        coverPath = this.coverPath,
        trackIds = this.trackIds,
        trackCount = this.trackCount,
        createdAt = this.createdAt
    )
}