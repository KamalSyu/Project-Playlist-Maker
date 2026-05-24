package com.practicum.playlistmaker.player.domain.model

import com.practicum.playlistmaker.core.models.domain.Playlist

data class PlaylistForPlayer(
    val id: String,
    val name: String,
    val coverPath: String?,
    val trackCount: Int,
) {
    companion object {
        fun fromDomain(playlist: Playlist): PlaylistForPlayer = PlaylistForPlayer(
            id = playlist.id,
            name = playlist.name,
            coverPath = playlist.coverPath,
            trackCount = playlist.trackCount,
        )
    }
}
