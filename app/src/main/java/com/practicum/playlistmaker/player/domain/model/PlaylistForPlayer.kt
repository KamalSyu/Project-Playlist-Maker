package com.practicum.playlistmaker.player.domain.model

import com.practicum.playlistmaker.core.models.Playlist

data class PlaylistForPlayer(
    val playlistId: String,
    val name: String,
    val coverPath: String?,
    val trackCount: Int,
    val trackIds: String?
) {
    companion object {
        fun fromDomain(playlist: Playlist): PlaylistForPlayer = PlaylistForPlayer(
            playlistId = playlist.id,
            name = playlist.name,
            coverPath = playlist.coverPath,
            trackCount = playlist.trackCount,
            trackIds = playlist.trackIds
        )
    }
}
