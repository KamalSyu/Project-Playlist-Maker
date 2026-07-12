package com.practicum.playlistmaker.mediateka.domain.usecase

import com.practicum.playlistmaker.core.models.Track
import com.practicum.playlistmaker.core.models.domain.AddTrackStatus
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia

class AddTrackToPlaylistUseCase(
    private val playlistsRepository: PlaylistsRepositoryMedia
) {
    suspend operator fun invoke(playlistId: Long, track: Track): AddTrackStatus {
        return try {
            playlistsRepository.addTrackToPlaylist(playlistId, track)
            AddTrackStatus.SUCCESS
        } catch (_: Exception) {
            AddTrackStatus.ERROR
        }
    }
}

