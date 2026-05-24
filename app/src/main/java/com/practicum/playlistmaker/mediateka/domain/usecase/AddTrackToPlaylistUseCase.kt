package com.practicum.playlistmaker.mediateka.domain.usecase

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.player.domain.repository.PlaylistRepository

class AddTrackToPlaylistUseCase(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, track: Track): AddTrackStatus {
        return playlistRepository.addTrackToPlaylist(playlistId.toString(), track)
    }
}
