package com.practicum.playlistmaker.mediateka.domain.usecase

import com.practicum.playlistmaker.core.models.domain.Playlist
import com.practicum.playlistmaker.mediateka.domain.repository.PlaylistsRepositoryMedia
import kotlinx.coroutines.flow.Flow

class LoadPlaylistsUseCase(
    private val playlistsRepositoryMedia: PlaylistsRepositoryMedia
) {
    operator fun invoke(): Flow<List<Playlist>> {
        return playlistsRepositoryMedia.getPlaylists()
    }
}
