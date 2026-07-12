
package com.practicum.playlistmaker.mediateka.domain.usecase

import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia

class RemoveTrackFromPlaylistUseCase(
    private val playlistsRepositoryMedia: PlaylistsRepositoryMedia
) {
    suspend operator fun invoke(playlistId: Long, trackId: String) {
        playlistsRepositoryMedia.removeTrackFromPlaylist(playlistId, trackId)
        playlistsRepositoryMedia.deleteTrackIfUnused(trackId)
    }
}