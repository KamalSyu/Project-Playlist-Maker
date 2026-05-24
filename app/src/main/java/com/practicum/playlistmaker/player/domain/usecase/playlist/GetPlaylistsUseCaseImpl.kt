package com.practicum.playlistmaker.player.domain.usecase.playlist

import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import com.practicum.playlistmaker.player.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class GetPlaylistsUseCaseImpl(
    private val playlistRepository: PlaylistRepository,
) : GetPlaylistsUseCase {

    override suspend operator fun invoke(): Flow<List<PlaylistForPlayer>> {
        return playlistRepository.getPlaylists()
    }
}
