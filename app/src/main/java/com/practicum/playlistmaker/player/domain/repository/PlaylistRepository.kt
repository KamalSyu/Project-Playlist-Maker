package com.practicum.playlistmaker.player.domain.repository

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.data.repository.AddTrackStatus
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer

interface PlaylistRepository {
    suspend fun getPlaylists(): List<PlaylistForPlayer>
    suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus
    suspend fun createPlaylist(name: String, coverPath: String? = null): String
}
