package com.practicum.playlistmaker.player.domain.usecase.playlist

import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer
import com.practicum.playlistmaker.player.domain.repository.PlaylistRepository

class GetPlaylistsUseCaseImpl(
    private val playlistRepository: PlaylistRepository,
) : GetPlaylistsUseCase {

    override suspend operator fun invoke(): List<PlaylistForPlayer> {
        return playlistRepository.getPlaylists()
    }
}
