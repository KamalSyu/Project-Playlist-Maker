package com.practicum.playlistmaker.player.domain.usecase.playlist

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.player.data.repository.AddTrackStatus
import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import com.practicum.playlistmaker.player.domain.repository.PlaylistRepository

class PlaylistInteractor(
    private val playlistRepository: PlaylistRepository
) {
    suspend fun getPlaylists(): List<PlaylistForPlayer> = playlistRepository.getPlaylists()

    suspend fun addTrackToPlaylist(playlistId: String, track: Track): AddTrackStatus =
        playlistRepository.addTrackToPlaylist(playlistId, track)

    suspend fun createPlaylist(name: String, coverPath: String? = null): Long =
        playlistRepository.createPlaylist(name, coverPath)

}
