package com.practicum.playlistmaker.player.domain.usecase.playlist

import com.practicum.playlistmaker.player.domain.model.PlaylistForPlayer


interface GetPlaylistsUseCase {
    suspend operator fun invoke(): List<PlaylistForPlayer>
}
