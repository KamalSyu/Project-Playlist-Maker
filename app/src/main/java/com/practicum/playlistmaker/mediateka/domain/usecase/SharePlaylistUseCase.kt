package com.practicum.playlistmaker.mediateka.domain.usecase

import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.data.db.PlaylistTrackEntity

class SharePlaylistUseCase {
    operator fun invoke(playlist: Playlist, tracks: List<PlaylistTrackEntity>): String {
        val header = buildString {
            appendLine(playlist.name)
            appendLine(playlist.description ?: "")
            appendLine("[${playlist.trackCount} треков]")
        }

        val body = tracks.mapIndexed { index, t ->
            val m = t.duration / 60
            val s = t.duration % 60
            "${index + 1}. ${t.artist} - ${t.title} (${m}:${s.toString().padStart(2, '0')})"
        }.joinToString("\n")

        return header + (if (body.isNotEmpty()) "\n$body" else "")
    }
}
