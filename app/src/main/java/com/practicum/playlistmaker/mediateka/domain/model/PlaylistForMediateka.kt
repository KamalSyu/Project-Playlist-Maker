package com.practicum.playlistmaker.mediateka.domain.model

import com.practicum.playlistmaker.core.models.Playlist

data class PlaylistForMediateka(
    val id: Long,
    val name: String,
    val description: String,
    val coverPath: String?,
    val trackIds: String,
    val trackCount: Int,
    val createdAt: Long
) {
    companion object {
        fun fromDomain(playlist: Playlist): PlaylistForMediateka = PlaylistForMediateka(
            id = playlist.id.toLongOrNull() ?: 0L,
            name = playlist.name,
            description = playlist.description ?: "",
            coverPath = playlist.coverPath,
            trackIds = playlist.trackIds ?: "[]",
            trackCount = playlist.trackCount,
            createdAt = playlist.createdAt
        )

        fun toDomain(playlistForMediateka: PlaylistForMediateka): Playlist = Playlist(
            id = playlistForMediateka.id.toString(),
            name = playlistForMediateka.name,
            description = playlistForMediateka.description,
            coverPath = playlistForMediateka.coverPath,
            trackIds = playlistForMediateka.trackIds,
            trackCount = playlistForMediateka.trackCount,
            createdAt = playlistForMediateka.createdAt
        )
    }
}
