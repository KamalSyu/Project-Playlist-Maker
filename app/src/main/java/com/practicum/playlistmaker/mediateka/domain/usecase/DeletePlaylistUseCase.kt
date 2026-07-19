package com.practicum.playlistmaker.mediateka.domain.usecase

import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia

class DeletePlaylistUseCase(
    private val playlistsRepositoryMedia: PlaylistsRepositoryMedia,
) {
    suspend operator fun invoke(playlistId: Long) {
        playlistsRepositoryMedia.deletePlaylistAndCleanup(playlistId)
    }
}
