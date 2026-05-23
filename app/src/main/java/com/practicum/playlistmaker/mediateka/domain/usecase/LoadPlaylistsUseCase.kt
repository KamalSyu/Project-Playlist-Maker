package com.practicum.playlistmaker.mediateka.domain.usecase

import androidx.lifecycle.LiveData
import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia

class LoadPlaylistsUseCase(
    private val playlistsRepositoryMedia: PlaylistsRepositoryMedia
) {
    operator fun invoke(): LiveData<List<Playlist>> {
        return playlistsRepositoryMedia.getPlaylists()
    }
}
