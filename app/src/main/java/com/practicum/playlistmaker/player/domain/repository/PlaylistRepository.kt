package com.practicum.playlistmaker.player.domain.repository

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylists(): Flow<List<PlaylistForPlayer>>
    suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus
    suspend fun createPlaylist(name: String, coverPath: String? = null, description: String? = null): String
}
